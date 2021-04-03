//
// Created by Matthew Good on 31/3/21.
//

#include <jni.h>
#include <AndroidDAW_SDK/JniHelpers/JniHelpers.h>
#include <libgen.h>
#include <filesystem>

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_basename(JNIEnv *env, jobject thiz,
                                                                        jstring path) {
    char * str = JniHelpers::Strings::newJniStringUTF(env, path);
    jstring obj = JniHelpers::Strings::newString(env, basename(str));
    JniHelpers::Strings::deleteJniStringUTF(&str);
    return JniHelpers::References::toGlobal(env, obj);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_dirname(JNIEnv *env, jobject thiz,
                                                                        jstring path) {
    char * str = JniHelpers::Strings::newJniStringUTF(env, path);
    jstring obj = JniHelpers::Strings::newString(env, dirname(str));
    JniHelpers::Strings::deleteJniStringUTF(&str);
    return JniHelpers::References::toGlobal(env, obj);
}

/**
 * resolves a given cwd and a given path to the directory
 * respecting trailing slashes
 *
 * we only care about unix based systems
 *
 * @param cwd the current directory
 * @param path the path to resolve, may be an absolute directory or a relative directory
 * @return the resolved directory
 */
std::string resolveDirname(char * cwd, char * path) {
    size_t cwd_len = strlen(cwd);
    size_t path_len = strlen(path);
    // 4 main cases
    // 1. cwd is empty, path is empty
    if (cwd_len == 0 && path_len == 0) {
        // assume / as root
        return std::string("/");
    }
    // 2. cwd is not empty, path is empty
    if (cwd_len != 0 && path_len == 0) {
        return std::string(cwd);
    }
    // 3. there are 3 sub-cases here
    {
        // 1. path is / itself, ignore cwd
        if (path_len == 1 && path[0] == '/') {
            return std::string("/");
        }
    }
    {
        // 2. path is an absolute directory, ignore cwd
        if (path[0] == '/') {
            if (path[path_len - 1] == '/') {
                path[path_len - 1] = '\0';
                return std::string(path);
            }
            return std::string(dirname(path));
        }
    }
    {
        // 3. path is a relative directory, cwd may be empty
        if (path[path_len-1] == '/') {
            path[path_len-1] = '\0';
        }
        // prepend root first, assume / as root
        std::string returnValue = std::string("/") + std::string(path);
        // if cwd is empty, return the path with the root prepended
        if (cwd_len == 0) return returnValue;
        return std::string(cwd) + returnValue;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_smallville7123_AndroidDAW_SDK_jni_1cpp_1api_JNI_1CPP_1API_00024Extras_resolvePath(JNIEnv *env,
                               jclass clazz,
                               jstring cwd,
                               jstring path) {
    char * cwd_ = JniHelpers::Strings::newJniStringUTF(env, cwd);
    char * path_ = JniHelpers::Strings::newJniStringUTF(env, path);

    std::string result = resolveDirname(cwd_, path_);

    JniHelpers::Strings::deleteJniStringUTF(&cwd_);
    JniHelpers::Strings::deleteJniStringUTF(&path_);

    jstring obj = JniHelpers::Strings::newString(env, result);
    return JniHelpers::References::toGlobal(env, obj);
}