//
// Created by Mac on 17/5/20.
//

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCInconsistentNamingInspection"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#ifndef CIRCULARBUFFER_JNIHELPERS_H
#define CIRCULARBUFFER_JNIHELPERS_H

#include <jni.h>
#include <deque>
#include <string>

class JniHelpers {
public:
    class References {
    public:
        static jobject toGlobal(JNIEnv * env, jobject obj);
        static jstring toGlobal(JNIEnv * env, jstring obj);
    };

    class Strings {
    public:

        static jclass charsetClass;
        static jmethodID forNameMethod;
        static jclass stringClass;
        static jmethodID charsetConstructor;

        /**
         * @return a std::string converted to a jbyteArray
         */
        static jbyteArray newByteArray(JNIEnv * env, const std::string & string);

        /**
         * @return a jbyteArray converted to a jstring
         */
        static jstring newString(JNIEnv * env, jbyteArray byteArray);

        /**
         * @return a const char* converted to a jstring
         */
        static jstring newString(JNIEnv * env, const char * string);

        /**
         * @return a std::string converted to a jstring
         */
        static jstring newString(JNIEnv * env, const std::string & string);

        /**
         * @return an allocated, null-terminated string, this must be freed by calling deleteJniStringUTF(&ReturnedString)
         *
         * @code // allocate a new jniString
         * char * jniString = newJniString(env, javaString);
         * // jniString is now allocated
         * // do something with the jniString
         * // de-allocate the jnistring
         * deleteJniString(&jniString);
         * // jniString is now de-allocated and set to nullptr
         * @endcode
         *
         * this version saves the string length inside the len parameter
         * @code size_t jniStringLength;
         * char * jniString = newJniString(env, javaString, &jniStringLength);
         * assert(jniString[jniStringLength] == '\0');
         * @endcode
         */
        static char *newJniStringUTF(JNIEnv *env, jstring from, size_t *len);

        /**
         * this is just
         * @code size_t unused;
         * return newJniStringUTF(env, from, &unused);
         * @endcode
         *
         * @return an allocated, null-terminated string, this must be freed by calling deleteJniStringUTF(&ReturnedString)
         *
         * @code // allocate a new jniString
         * char * jniString = newJniString(env, javaString);
         * // jniString is now allocated
         * // do something with the jniString
         * // de-allocate the jnistring
         * deleteJniString(&jniString);
         * // jniString is now de-allocated and set to nullptr
         * @endcode
         */
        static char *newJniStringUTF(JNIEnv *env, jstring from);

        /**
         * de-allocates a jniString returned by newJniStringUTF
         * @param string the jniString to de-allocate
         *
         * @code // allocate a new jniString
         * char * jniString = newJniString(env, javaString);
         * // jniString is now allocated
         * // do something with the jniString
         * // de-allocate the jnistring
         * deleteJniString(&jniString);
         * // jniString is now de-allocated and set to nullptr
         * @endcode
         */
        static void deleteJniStringUTF(char **string);

        /**
         * @return the length of a java string
         *
         * the code below is used internally in newJniStringUTF(JNIEnv *env, jstring from, size_t * len)
         *
         * @code size_t len = jniStrlenUTF(env, from);
         */
        static size_t jniStrlenUTF(JNIEnv *env, jstring from);

        static void copyJniStringUTF(JNIEnv *env, jstring from, char *to);
    };

    class Exceptions {
    public:
        static int throwException(JNIEnv *env, const char *exception, const char *message);

        static int throwRuntimeException(JNIEnv *env, const char *message);
    };

    class Arrays {
    public:
        static jintArray createJniIntArray(JNIEnv *env, size_t size);

        static void setJniIntArrayIndex(JNIEnv *env, jintArray *array, int index, int value);

        static bool setJniIntArrayIndexes(JNIEnv *env, jintArray *array, int index, int *pointer,
                                          size_t totalIndexesInPointer);
    };

    class Jvm {
    public:
        static JavaVM *getJvm(JNIEnv *env);

        static void destroyJvm(JavaVM *JVM);
    };

    class Types {
    public:
        const char * Boolean = "Z";
        const char * Byte = "B";
        const char * Char = "C";
        const char * Short = "S";
        const char * Int = "I";
        const char * Long = "J";
        const char * Float = "F";
        const char * Double = "D";
    };

    class Classes {
    public:

        class Internal {
        public:
            class JavaClassMember {
            public:
                JavaClassMember(const char *type, const char *parameter,
                                void *value);

                const char *type;
                const char *parameter;
                void *value;
            };

            class JavaClassConverter {
            public:

                Types types = Types();

                JavaClassConverter();

                void addBooleanMember(const char *parameter, void *value);

                void read(JNIEnv *env, const char * className, jobject classInstance);
                void clean();

                std::deque<JniHelpers::Classes::Internal::JavaClassMember> * javaClassMembers = nullptr;

            private:
                const char *targetClass = nullptr;
            };
        };

        typedef JniHelpers::Classes::Internal::JavaClassConverter JavaClassConverter;

        static JavaClassConverter *newJavaClassConverter();
    };

    class Methods {
    public:

    };
};

#endif //CIRCULARBUFFER_JNIHELPERS_H

#pragma clang diagnostic pop