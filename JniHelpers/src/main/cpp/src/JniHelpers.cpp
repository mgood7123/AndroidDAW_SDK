//
// Created by Mac on 17/5/20.
//

#include <cstring>
#include <cstdlib>
#include <deque>
#include "../include/AndroidDAW_SDK/JniHelpers/JniHelpers.h"

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCInconsistentNamingInspection"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"

jobject JniHelpers::References::toGlobal(JNIEnv * env, jobject obj) {
    return env->NewGlobalRef(obj);
}

jstring JniHelpers::References::toGlobal(JNIEnv * env, jstring obj) {
    return static_cast<jstring>(env->NewGlobalRef(obj));
}

jclass JniHelpers::Strings::charsetClass = nullptr;
jclass JniHelpers::Strings::stringClass = nullptr;
jmethodID JniHelpers::Strings::forNameMethod = nullptr;
jmethodID JniHelpers::Strings::charsetConstructor = nullptr;

jbyteArray JniHelpers::Strings::newByteArray(JNIEnv * env, const std::string & string) {
    size_t byteCount = string.length();
    jbyteArray bytes = env->NewByteArray(byteCount);
    env->SetByteArrayRegion(
            bytes,
            0,
            byteCount,
            reinterpret_cast<const jbyte*>(string.c_str())
    );
    return bytes;
}

jstring JniHelpers::Strings::newString(JNIEnv * env, jbyteArray byteArray) {
    if (charsetClass == nullptr) {
        charsetClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/nio/charset/Charset")));
    }
    if (stringClass == nullptr) {
        stringClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/String")));
    }
    if (forNameMethod == nullptr) {
        forNameMethod = env->GetStaticMethodID(
                        charsetClass, "forName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;");
    }
    if (charsetConstructor == nullptr) {
        charsetConstructor = env->GetMethodID(
                stringClass, "<init>", "([BLjava/nio/charset/Charset;)V");
    }

    jstring utf8 = env->NewStringUTF("UTF-8");
    jobject charset = env->CallStaticObjectMethod(charsetClass, forNameMethod, utf8);
    jobject obj = env->NewObject(stringClass, charsetConstructor, byteArray, charset);
    return reinterpret_cast<jstring>(obj);
}

jstring JniHelpers::Strings::newString(JNIEnv * env, const char * string) {
    return env->NewStringUTF(string);
}

jstring JniHelpers::Strings::newString(JNIEnv * env, const std::string & string) {
    return newString(env, string.c_str());
}

char * JniHelpers::Strings::newJniStringUTF(JNIEnv *env, jstring from, size_t * len) {
    // TODO: error check
    *len = jniStrlenUTF(env, from);
    char * ret = new char[*len];
    jboolean val;
    const char * tmp = env->GetStringUTFChars(from, &val);
    memcpy(ret, tmp, *len);
    ret[*len] = '\0';
    env->ReleaseStringUTFChars(from, tmp);
    return ret;
}

char *JniHelpers::Strings::newJniStringUTF(JNIEnv *env, jstring from) {
    size_t unused;
    return newJniStringUTF(env, from, &unused);
}

void JniHelpers::Strings::deleteJniStringUTF(char ** string) {
    char * stringToDelete = *string;
    delete[] stringToDelete;
    *string = nullptr;
}

size_t JniHelpers::Strings::jniStrlenUTF(JNIEnv *env, jstring from) {
    // TODO: error check
    return static_cast<size_t>(env->GetStringUTFLength(from));
}

void JniHelpers::Strings::copyJniStringUTF(JNIEnv *env, jstring from, char * to) {
    size_t len;
    char * str = newJniStringUTF(env, from, &len);
    memcpy(to, str, len);
    deleteJniStringUTF(&str);
}

int JniHelpers::Exceptions::throwException(JNIEnv* env, const char * exception, char const* message) {
    // TODO: error check
    jclass exClass = env->FindClass(exception);
    env->ThrowNew(exClass, message);
    return -1;
}

int JniHelpers::Exceptions::throwRuntimeException(JNIEnv* env, char const* message) {
    return throwException(env, "java/lang/RuntimeException", message);
}

jintArray JniHelpers::Arrays::createJniIntArray(JNIEnv *env, size_t size) {
    // TODO: error check
    jintArray result = env->NewIntArray(size);
    if (result == NULL) {
        return NULL; /* out of memory error thrown */
    } else return result;
}

void JniHelpers::Arrays::setJniIntArrayIndex(JNIEnv *env, jintArray * array, int index, int value) {
    if (array == NULL) return;
    // fill a temp structure to use to populate the java int array
    jint fill[1];

    // populate the values
    fill[0] = value;

    // move from the temp structure to the java structure
    env->SetIntArrayRegion(*array, index, 1, fill);
}

bool JniHelpers::Arrays::setJniIntArrayIndexes(
        JNIEnv *env, jintArray * array, int index,
        int * pointer, size_t totalIndexesInPointer
) {
    if (array == NULL) return false;
    // fill a temp structure to use to populate the java int array
    jint * fill = new jint[totalIndexesInPointer];
    if (fill == nullptr) return false;

    // populate the values
    // if valueTotalIndexes is 1, then
    for (int i = 0; i < totalIndexesInPointer; ++i) {
        fill[i] = pointer[i];
    }
//    memcpy(fill, pointer, totalIndexesInPointer);
//    env->SetIntArrayRegion(*array, index, totalIndexesInPointer, pointer);

    // move from the temp structure to the java structure
    env->SetIntArrayRegion(*array, index, totalIndexesInPointer, fill);
    delete[] fill;
    return true;
}

JavaVM * JniHelpers::Jvm::getJvm(JNIEnv *env) {
    // TODO: error check
    JavaVM * JVM;
    jint ret = env->GetJavaVM(&JVM);
    return JVM;
}

void JniHelpers::Jvm::destroyJvm(JavaVM * JVM) {
    // TODO: error check
    jint ret = JVM->DestroyJavaVM();
}

JniHelpers::Classes::Internal::JavaClassMember::JavaClassMember(
        const char *type, const char *parameter, void *value
) {
    this->type = type;
    this->parameter = parameter;
    this->value = value;
}

JniHelpers::Classes::Internal::JavaClassConverter::JavaClassConverter() {}

JniHelpers::Classes::JavaClassConverter * JniHelpers::Classes::newJavaClassConverter() {
    JniHelpers::Classes::JavaClassConverter * JCC = new JavaClassConverter();
    JCC->javaClassMembers = new std::deque<JniHelpers::Classes::Internal::JavaClassMember>;
    return JCC;
}

void JniHelpers::Classes::Internal::JavaClassConverter::addBooleanMember(
        const char *parameter, void *value
) {
    javaClassMembers->push_front(JavaClassMember(types.Boolean, parameter, value));
}

void JniHelpers::Classes::Internal::JavaClassConverter::read(
        JNIEnv *env, const char * className, jobject classInstance
) {
    jclass c = env->FindClass(className);
    size_t size = javaClassMembers->size();
    for (int i = 0; i < size; i++) {
        JavaClassMember member = javaClassMembers[0][i];
        jfieldID id = env->GetFieldID(c, member.parameter, member.type);
        if (member.type[0] == types.Boolean[0])
            *static_cast<jboolean *>(member.value) = env->GetBooleanField(classInstance, id);
        else if (member.type[0] == types.Byte[0])
            *static_cast<jbyte *>(member.value) = env->GetByteField(classInstance, id);
        else if (member.type[0] == types.Char[0])
            *static_cast<jchar *>(member.value) = env->GetCharField(classInstance, id);
        else if (member.type[0] == types.Short[0])
            *static_cast<jshort *>(member.value) = env->GetShortField(classInstance, id);
        else if (member.type[0] == types.Int[0])
            *static_cast<jint *>(member.value) = env->GetIntField(classInstance, id);
        else if (member.type[0] == types.Long[0])
            *static_cast<jlong *>(member.value) = env->GetLongField(classInstance, id);
        else if (member.type[0] == types.Float[0])
            *static_cast<jfloat *>(member.value) = env->GetFloatField(classInstance, id);
        else if (member.type[0] == types.Double[0])
            *static_cast<jdouble *>(member.value) = env->GetDoubleField(classInstance, id);
        else JniHelpers::Exceptions::throwRuntimeException(env, "type not found");
    }
}

void JniHelpers::Classes::Internal::JavaClassConverter::clean() {
    while (!javaClassMembers->empty()) javaClassMembers->pop_front();
}

#pragma clang diagnostic pop