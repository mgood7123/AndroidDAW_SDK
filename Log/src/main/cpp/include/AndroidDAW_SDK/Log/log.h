//
// Created by matthew good on 20/11/20.
//

#ifndef AndroidDAW_SDK_LOG_H
#define AndroidDAW_SDK_LOG_H

#include <android/log.h>

#include <cstdlib> // abort

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "AndroidDAW_SDK", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AndroidDAW_SDK", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AndroidDAW_SDK", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "AndroidDAW_SDK", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AndroidDAW_SDK", __VA_ARGS__)
#define LOGF(...) { \
__android_log_print(ANDROID_LOG_FATAL, "AndroidDAW_SDK", __VA_ARGS__); \
abort(); \
}

#define LOGA_NO_REASON "no reason given"

#define LOGA_NO_ARGS(bool_condition, fmt) { \
if (!(bool_condition)) \
LOGF(\
"%s:%d: %s: Assertion '%s' failed. Additional Assertion information: " fmt, \
__FILE__, __LINE__, __FUNCTION__, \
#bool_condition \
); \
}

#define LOGA_WITH_ARGS(bool_condition, fmt, ...) { \
if (!(bool_condition)) \
LOGF(\
"%s:%d: %s: Assertion '%s' failed. Additional Assertion information: " fmt, \
__FILE__, __LINE__, __FUNCTION__, \
#bool_condition, __VA_ARGS__ \
); \
}

#endif //AAUDIOTRACK_LOG_H
