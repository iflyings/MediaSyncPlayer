#ifndef _APP_CONF_H_
#define _APP_CONF_H_

#include <android/log.h>

#define  TAG                                 "zw"
#define  LOGI(...)                          __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define  LOGE(...)                          __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define  LOGD(...)                          __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define  LOGW(...)                          __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

#endif
