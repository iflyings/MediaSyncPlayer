//
// Created by iflyings on 20-12-17.
//
#include <jni.h>
#include <errno.h>
#include <assert.h>
#include "native_codec.h"

static struct {
    JavaVM*     mJavaVM;
    jclass      mClazz;
    jfieldID    mNativeContext;
    jmethodID   mNativeCallback;
} gFields;

struct NativePlayer {
    jobject mJavaObject;
    NativeCodecManager *manager;
};

typedef struct NativePlayer NativePlayer;

static void throwCodecException(JNIEnv *env,
        int err, int32_t actionCode, const char *msg = nullptr) {

    jclass clazz = env->FindClass("com/android/iflyings/nativeplayer/NativePlayer$CodecException");
    assert(clazz != nullptr);

    const jmethodID ctor = env->GetMethodID(clazz, "<init>", "(IILjava/lang/String;)V");
    assert(ctor != nullptr);

    jstring msgObj = env->NewStringUTF(msg != nullptr ? msg : "CodecException");
    auto exception = (jthrowable) env->NewObject(clazz, ctor, err, actionCode, msgObj);
    env->Throw(exception);

    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(msgObj);
}

static void throwException(JNIEnv* env, const char* exceptionName, const char* error) {
    jclass clazz = env->FindClass(exceptionName);
    assert(clazz != nullptr);

    env->ThrowNew(clazz, error);
    /*
     * This is usually not necessary -- local references are released
     * automatically when the native code returns to the VM.  It's
     * required if the code doesn't actually return, e.g. it's sitting
     * in a native event loop.
     */
    env->DeleteLocalRef(clazz);

}

static void nativeCallback(int type, int action, void *data)
{
    JNIEnv* env;
    auto player = (NativePlayer *)data;
    if (JNI_OK == gFields.mJavaVM->AttachCurrentThread(&env, NULL)) {
        if (env != NULL) {
            env->CallVoidMethod(player->mJavaObject, gFields.mNativeCallback, type, action);
        }
        gFields.mJavaVM->DetachCurrentThread();
    }
}

static NativePlayer *getManager(JNIEnv* env, jobject thiz) {
    auto *manager = reinterpret_cast<NativePlayer *>(env->GetLongField(thiz, gFields.mNativeContext));
    if (manager == NULL) {
        throwException(env, "java/lang/IllegalStateException", "NativeCodecManager is NULL");
    }
    return manager;
}
static void setManager(JNIEnv* env, jobject thiz, NativePlayer *player) {
    auto *old = reinterpret_cast<NativePlayer *>(env->GetLongField(thiz, gFields.mNativeContext));
    delete old;
    env->SetLongField(thiz, gFields.mNativeContext, reinterpret_cast<jlong>(player));
}


extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_setSoftwareCodec(JNIEnv *env, jobject thiz, jboolean soft) {
    NativeCodec_setSoftwareCodec(getManager(env, thiz)->manager, soft);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_setSurface(JNIEnv *env, jobject thiz,
        jobject surface) {
    NativeCodec_setSurface(getManager(env, thiz)->manager, env, surface);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_setDataSource(JNIEnv *env, jobject thiz,
        jstring path) {
    const char *utf8 = env->GetStringUTFChars(path, NULL);
    NativeCodec_setPath(getManager(env, thiz)->manager, utf8);
    env->ReleaseStringUTFChars(path, utf8);
}

extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_create(JNIEnv *env, jobject thiz) {
    if (gFields.mNativeContext == NULL) {
        gFields.mClazz = env->GetObjectClass(thiz);
        gFields.mNativeContext = env->GetFieldID(gFields.mClazz, "mNativeContext", "J");
        if (gFields.mNativeContext == NULL) {
            throwException(env, "java/lang/IllegalStateException", "can not find field mNativeContext");
        }
        gFields.mNativeCallback = env->GetMethodID(gFields.mClazz,"onNativeCallBack","(II)V");
        if (gFields.mNativeCallback == NULL) {
            throwException(env, "java/lang/IllegalStateException", "can not find method onNativeCallBack");
        }
        env->GetJavaVM(&gFields.mJavaVM);
    }
    auto *player = new NativePlayer();
    player->mJavaObject = env->NewGlobalRef(thiz);
    player->manager = NativeCodec_create(player);
    NativeCodec_setCallback(player->manager, nativeCallback);
    setManager(env, thiz, player);

}
extern "C" JNIEXPORT jint JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_prepare(JNIEnv *env, jobject thiz) {
    return NativeCodec_prepare(getManager(env, thiz)->manager);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_renderOneFrame(JNIEnv *env, jobject thiz) {
    NativeCodec_renderOneFrame(getManager(env, thiz)->manager);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_start(JNIEnv *env, jobject thiz) {
    NativeCodec_start(getManager(env, thiz)->manager);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_pause(JNIEnv *env, jobject thiz) {
    NativeCodec_pause(getManager(env, thiz)->manager);
}
extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_nativeplayer_NativePlayer_release(JNIEnv *env, jobject thiz) {
    auto *player = getManager(env, thiz);
    NativeCodec_release(player->manager);
    env->DeleteGlobalRef(player->mJavaObject);
    setManager(env, thiz, NULL);
}