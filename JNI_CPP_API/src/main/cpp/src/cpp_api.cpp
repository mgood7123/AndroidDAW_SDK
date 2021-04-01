//
// Created by Matthew Good on 31/3/21.
//

#include <jni.h>
#include <AndroidDAW_SDK/JniHelpers/JniHelpers.h>
#include <libgen.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_basename(JNIEnv *env, jobject thiz,
                                                                        jstring path) {
    const char * str = JniHelpers::Strings::newJniStringUTF(env, path);
    jstring obj = JniHelpers::Strings::newString(env, basename(str));
    JniHelpers::Strings::deleteJniStringUTF(&str);
    return JniHelpers::References::toGlobal(env, obj);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_dirname(JNIEnv *env, jobject thiz,
                                                                        jstring path) {
    const char * str = JniHelpers::Strings::newJniStringUTF(env, path);
    jstring obj = JniHelpers::Strings::newString(env, dirname(str));
    JniHelpers::Strings::deleteJniStringUTF(&str);
    return JniHelpers::References::toGlobal(env, obj);
}

std::string resolvePathInternal(const char * cwd, const char * path) {
    return "";
}

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_00024Extras_resolvePath(JNIEnv *env,
                               jclass clazz,
                               jstring cwd,
                               jstring path) {
    const char * cwd_ = JniHelpers::Strings::newJniStringUTF(env, cwd);
    const char * path_ = JniHelpers::Strings::newJniStringUTF(env, path);

    std::string result = resolvePathInternal(cwd_, path_);

    JniHelpers::Strings::deleteJniStringUTF(&cwd_);
    JniHelpers::Strings::deleteJniStringUTF(&path_);

    jstring obj = JniHelpers::Strings::newString(env, result);
    return JniHelpers::References::toGlobal(env, obj);
}