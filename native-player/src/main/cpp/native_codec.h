//
// Created by iflyings on 20-12-18.
//

#ifndef MEDIASYNCPLAYER_NATIVE_CODEC_H
#define MEDIASYNCPLAYER_NATIVE_CODEC_H

struct NativeCodecManager;
typedef struct NativeCodecManager NativeCodecManager;

typedef void (*Callback)(int type, int action, void *data);

NativeCodecManager* NativeCodec_create(void *data);

void NativeCodec_setCallback(NativeCodecManager *pManager, Callback callback);

void NativeCodec_setSoftwareCodec(NativeCodecManager *pManager, bool soft);

void NativeCodec_setPath(NativeCodecManager* manager, const char *path);

void NativeCodec_setSurface(NativeCodecManager* manager, JNIEnv* env, jobject surface);

int NativeCodec_prepare(NativeCodecManager* manager);

void NativeCodec_renderOneFrame(NativeCodecManager* manager);

void NativeCodec_start(NativeCodecManager* manager);

void NativeCodec_pause(NativeCodecManager* manager);

void NativeCodec_release(NativeCodecManager* manager);



#endif //MEDIASYNCPLAYER_NATIVE_CODEC_H
