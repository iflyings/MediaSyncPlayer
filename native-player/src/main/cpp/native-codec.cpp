/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* This is a JNI example where we use native methods to play video
 * using the native AMedia* APIs.
 * See the corresponding Java source file located at:
 *
 *   src/com/example/nativecodec/NativeMedia.java
 *
 * In this example we use assert() for "impossible" error conditions,
 * and explicit handling and recovery for more likely error conditions.
 */

#include <assert.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

#include "media/NdkMediaCodec.h"
#include "media/NdkMediaExtractor.h"

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>
#define TAG "NativeCodec"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// for native window JNI
#include <android/native_window_jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <pthread.h>

#include "looper.h"
#include "native_codec.h"

typedef enum {
    kStateCreate,
    kStateStart,
    kStatePause,
    kStateOver,
    kStateError,
} PlayerState;

struct NativeCodecManager {
    char *path;
    jobject surface;
    bool software;
    Callback callback;

    ANativeWindow* window;
    AMediaExtractor* extractor;
    AMediaCodec *codec;
    looper *looper;

    int64_t renderStartTickUs;
    bool sawInputEOS;
    bool sawOutputEOS;
    bool renderOnce;
    PlayerState state;
    void *data;
};

enum {
    kMsgCodecBuffer,
    kMsgRenderOneFrame,
    kMsgPause,
    kMsgStart,
    kMsgSeek,
    kMsgDecodeRelease,
};

class mylooper: public looper {
public:
    mylooper() = default;
private:
    void handle(int what, void* obj) override;
};

static int64_t systemTimeUs() {
    timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return now.tv_sec * 1000000LL + now.tv_nsec / 1000;
}

static void doCodecWork(NativeCodecManager *manager) {
    if (manager->state != kStateStart) {
        return;
    }
    if (!manager->sawInputEOS) {
        ssize_t buf_idx = AMediaCodec_dequeueInputBuffer(manager->codec, 2000);
        LOGV("input buffer %zd", buf_idx);
        if (buf_idx >= 0) {
            size_t buf_size;
            auto buf = AMediaCodec_getInputBuffer(manager->codec, buf_idx, &buf_size);

            auto sampleSize = AMediaExtractor_readSampleData(manager->extractor, buf, buf_size);
            if (sampleSize < 0) {
                sampleSize = 0;
                manager->sawInputEOS = true;
                LOGV("EOS");
            }
            auto presentationTimeUs = AMediaExtractor_getSampleTime(manager->extractor);

            AMediaCodec_queueInputBuffer(manager->codec, buf_idx, 0, sampleSize, presentationTimeUs,
                                         manager->sawInputEOS ? AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM : 0);
            AMediaExtractor_advance(manager->extractor);
        }
    }

    if (!manager->sawOutputEOS) {
        AMediaCodecBufferInfo info;
        auto status = AMediaCodec_dequeueOutputBuffer(manager->codec, &info, 0);
        if (status >= 0) {
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
                LOGV("output EOS");
                manager->sawOutputEOS = true;
            }
            if (manager->renderStartTickUs <= 0) {
                manager->renderStartTickUs = systemTimeUs() - info.presentationTimeUs;
            }
            int64_t delay = (manager->renderStartTickUs + info.presentationTimeUs) - systemTimeUs();
            if (delay > 0) {
                usleep(delay / 1000);
            }
            AMediaCodec_releaseOutputBuffer(manager->codec, status, info.size != 0);
            if (manager->renderOnce) {
                manager->renderOnce = false;
                manager->state = kStatePause;
                return;
            }
        } else if (status == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
            LOGV("output buffers changed");
        } else if (status == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            auto format = AMediaCodec_getOutputFormat(manager->codec);
            LOGV("format changed to: %s", AMediaFormat_toString(format));
            AMediaFormat_delete(format);
        } else if (status == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
            LOGV("no output buffer right now");
        } else {
            LOGV("unexpected info code: %zd", status);
        }
    }

    if (!manager->sawInputEOS || !manager->sawOutputEOS) {
        manager->looper->post(kMsgCodecBuffer, manager);
    } else {
        manager->callback(0, 0, manager->data);
    }
}

static void doCodecRelease(NativeCodecManager *manager) {
    if (manager->extractor != NULL) {
        AMediaExtractor_delete(manager->extractor);
        manager->extractor = NULL;
    }
    if (manager->codec != NULL) {
        AMediaCodec_stop(manager->codec);
        AMediaCodec_delete(manager->codec);
        manager->codec = NULL;
    }
    if (manager->window != NULL) {
        ANativeWindow_release(manager->window);
        manager->window = NULL;
    }
    if (manager->path != NULL) {
        delete [] manager->path;
        manager->path = NULL;
    }
}

static void doCodecPause(NativeCodecManager *manager) {
    if (manager->state != kStateStart && manager->state != kStatePause) {
        return;
    }
    manager->state = kStatePause;
    manager->looper->clean();
}

static void doCodecRenderOneFrame(NativeCodecManager *manager) {
    if (manager->state == kStatePause || manager->state == kStateStart) {
        manager->looper->clean();
        manager->renderOnce = true;
        manager->looper->post(kMsgCodecBuffer, manager);
    }
}

static void doCodecStart(NativeCodecManager *manager) {
    if (manager->state == kStateStart || manager->state == kStatePause) {
        manager->state = kStateStart;
        manager->looper->post(kMsgCodecBuffer, manager);
    }
}

static void doCodecSeek(NativeCodecManager *manager) {
    if (manager->state != kStateStart && manager->state != kStatePause) {
        return;
    }
    AMediaExtractor_seekTo(manager->extractor, 0, AMEDIAEXTRACTOR_SEEK_PREVIOUS_SYNC);
    AMediaCodec_flush(manager->codec);
    manager->sawInputEOS = false;
    manager->sawOutputEOS = false;
}

void mylooper::handle(int what, void* obj) {
    switch (what) {
        case kMsgCodecBuffer: {
            doCodecWork((NativeCodecManager *)obj);
            break;
        }
        case kMsgDecodeRelease: {
            doCodecRelease((NativeCodecManager*)obj);
            break;
        }
        case kMsgSeek: {
            doCodecSeek((NativeCodecManager*)obj);
            break;
        }
        case kMsgPause:
        {
            doCodecPause((NativeCodecManager*)obj);
            break;
        }
        case kMsgStart:
        {
            doCodecStart((NativeCodecManager*)obj);
            break;
        }
        default:{
            break;
        }
    }
}

int NativeCodec_prepare(NativeCodecManager* manager)
{
    int i;
    int num_tracks;
    media_status_t err;

    if (manager->path == NULL || manager->window == NULL) {
        goto ERR_1;
    }

    manager->extractor = AMediaExtractor_new();
    err = AMediaExtractor_setDataSource(manager->extractor, manager->path);
    if (err != AMEDIA_OK) {
        LOGV("setDataSource error: %d", err);
        goto ERR_2;
    }

    num_tracks = AMediaExtractor_getTrackCount(manager->extractor);

    LOGV("input has %d tracks", num_tracks);
    for (i = 0; i < num_tracks; i++) {
        AMediaFormat *format = AMediaExtractor_getTrackFormat(manager->extractor, i);
        const char *s = AMediaFormat_toString(format);
        LOGV("track %d format: %s", i, s);
        const char *mime;
        if (!AMediaFormat_getString(format, AMEDIAFORMAT_KEY_MIME, &mime)) {
            LOGV("no mime type");
            continue;
        } else if (!strncmp(mime, "video/", 6)) {
            // Omitting most error handling for clarity.
            // Production code should check for errors.
            err = AMediaExtractor_selectTrack(manager->extractor, i);
            if (err != AMEDIA_OK) {
                LOGV("selectTrack error: %d", err);
                AMediaFormat_delete(format);
                goto ERR_2;
            }
            manager->codec = AMediaCodec_createDecoderByType(mime);
            if (manager->codec == nullptr) {
                LOGV("createDecoder mime: %s", mime);
                AMediaFormat_delete(format);
                goto ERR_2;
            }
            err = AMediaCodec_configure(manager->codec, format, manager->window, nullptr, 0);
            if (err != AMEDIA_OK) {
                LOGV("codec configure: %d", err);
                AMediaFormat_delete(format);
                goto ERR_3;
            }
        }
        AMediaFormat_delete(format);
    }
    if (manager->codec == nullptr) {
        goto ERR_2;
    }

    err = AMediaCodec_start(manager->codec);
    if (err != AMEDIA_OK) {
        goto ERR_3;
    }

    manager->looper = new mylooper();
    if (manager->looper == NULL) {
        goto ERR_4;
    }
    if (0 != manager->looper->open()) {
        goto ERR_5;
    }
    manager->state = kStatePause;
    return 0;
ERR_5:
    delete manager->looper;
    manager->looper = NULL;
ERR_4:
    AMediaCodec_stop(manager->codec);
ERR_3:
    AMediaCodec_delete(manager->codec);
    manager->codec = NULL;
ERR_2:
    AMediaExtractor_delete(manager->extractor);
    manager->extractor = NULL;
ERR_1:
    manager->state = kStateError;
    return -1;
}

void NativeCodec_release(NativeCodecManager* manager)
{
    if (manager->looper != NULL) {
        manager->looper->clean();
        manager->looper->post(kMsgDecodeRelease, manager);
        manager->looper->closeWait();
        delete manager->looper;
    } else {
        doCodecRelease(manager);
    }
}

void NativeCodec_renderOneFrame(NativeCodecManager* manager)
{
    manager->looper->post(kMsgRenderOneFrame, manager);
}

void NativeCodec_start(NativeCodecManager* manager)
{
    manager->looper->post(kMsgStart, manager);
}

void NativeCodec_pause(NativeCodecManager* manager)
{
    manager->looper->post(kMsgPause, manager);
}

void NativeCodec_setSoftwareCodec(NativeCodecManager *manager, bool soft)
{
    manager->software = soft;
}

void NativeCodec_setPath(NativeCodecManager* manager, const char *path)
{
    if (manager->path != NULL) {
        delete [] manager->path;
    }
    manager->path = new char[strlen(path) + 1];
    strcpy(manager->path, path);
}

void NativeCodec_setSurface(NativeCodecManager* manager, JNIEnv* env, jobject surface)
{
    // obtain a native window from a Java surface
    if (manager->window) {
        ANativeWindow_release(manager->window);
    }
    manager->window = ANativeWindow_fromSurface(env, surface);
}

void NativeCodec_setCallback(NativeCodecManager *manager, Callback callback)
{
    manager->callback = callback;
}

NativeCodecManager* NativeCodec_create(void *data)
{
    auto *manager = new NativeCodecManager();
    manager->state = kStateCreate;
    manager->data = data;
    return manager;
}