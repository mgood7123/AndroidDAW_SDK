cmake_minimum_required(VERSION 3.4.1)

# empty macro, for checking import of cmake file
macro(AndroidDAW_SDK_CMAKE_HELPER)
endmacro()

macro(print_variable_AndroidDAW_SDK variable)
    message(STATUS "${variable} = ${${variable}}")
endmacro()

macro(set_AndroidDAW_SDK_ROOT AndroidDAW_SDK)
    set(AndroidDAW_SDK_ROOT ${AndroidDAW_SDK})
    set(PREFIX_PARENT_AndroidDAW_SDK EXPORTED_LIBRARIES)
    set(LIBRARY_PATH_AndroidDAW_SDK ${PREFIX_PARENT_AndroidDAW_SDK}/${CMAKE_ANDROID_ARCH_ABI})
#    print_variable_AndroidDAW_SDK(AndroidDAW_SDK_ROOT)
#    print_variable_AndroidDAW_SDK(PREFIX_PARENT_AndroidDAW_SDK)
#    print_variable_AndroidDAW_SDK(LIBRARY_PATH_AndroidDAW_SDK)
endmacro()

macro(set_module_name_AndroidDAW_SDK module_name)
    set(AndroidDAW_SDK_MODULE_NAME ${module_name})
    set(AndroidDAW_SDK_MODULE_NAME_ROOT ${AndroidDAW_SDK_ROOT}/${AndroidDAW_SDK_MODULE_NAME})
    set(AndroidDAW_SDK_LIBRARY_EXPORT_PREFIX ${AndroidDAW_SDK_MODULE_NAME_ROOT}/${LIBRARY_PATH_AndroidDAW_SDK})
    set(AndroidDAW_SDK_MODULE_TARGET AndroidDAW_${AndroidDAW_SDK_MODULE_NAME})
#    print_variable_AndroidDAW_SDK(AndroidDAW_SDK_MODULE_NAME)
#    print_variable_AndroidDAW_SDK(AndroidDAW_SDK_MODULE_NAME_ROOT)
#    print_variable_AndroidDAW_SDK(AndroidDAW_SDK_LIBRARY_EXPORT_PREFIX)
#    print_variable_AndroidDAW_SDK(AndroidDAW_SDK_MODULE_TARGET)
endmacro()

macro(AndroidDAW_SDK_CMAKE_HELPER_import_header SDK)
    include_directories(${AndroidDAW_SDK_ROOT}/${SDK}/src/main/cpp/include)
endmacro()

macro(AndroidDAW_SDK_CMAKE_HELPER_import_library SDK)
    add_library(AndroidDAW_${SDK} SHARED IMPORTED)
    set_target_properties(AndroidDAW_${SDK}
            PROPERTIES IMPORTED_LOCATION
            ${AndroidDAW_SDK_ROOT}/${SDK}/${LIBRARY_PATH_AndroidDAW_SDK}/libAndroidDAW_${SDK}.so
    )
endmacro()

macro(import_library__AndroidDAW_Midi)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(Midi)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(Midi)
endmacro()

macro(import_headers__AndroidDAW_SDK__RingBuffer)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(RingBuffer)
endmacro()

macro(import_headers__AndroidDAW_SDK__Log)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(Log)
endmacro()

macro(import_library__AndroidDAW_Plugin)
    import_headers__AndroidDAW_SDK__Log()
    import_headers__AndroidDAW_SDK__RingBuffer()
    message(STATUS "in ${CMAKE_CURRENT_LIST_FILE}:")
    message(STATUS "    please remember to add `import_library__AndroidDAW_Midi()`")
    message(STATUS "    after `import_library__AndroidDAW_Plugin()`")
    message(STATUS "    (or before if you are alphabetical ordering)")
    message(STATUS "    and then link to `AndroidDAW_Midi`")
    AndroidDAW_SDK_CMAKE_HELPER_import_header(Plugin)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(Plugin)
endmacro()

macro(import_library__AndroidDAW_JniHelpers)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(JniHelpers)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(JniHelpers)
endmacro()

macro(import_library__AndroidDAW_JNI_CPP_API)
    message(STATUS "in ${CMAKE_CURRENT_LIST_FILE}:")
    message(STATUS "    please remember to add `import_library__AndroidDAW_JniHelpers()`")
    message(STATUS "    after `import_library__AndroidDAW_JNI_CPP_API()`")
    message(STATUS "    and then link to `AndroidDAW_JniHelpers`")
    AndroidDAW_SDK_CMAKE_HELPER_import_header(JNI_CPP_API)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(JNI_CPP_API)
endmacro()

macro(export_library_AndroidDAW_SDK)
    add_custom_command(
            TARGET ${AndroidDAW_SDK_MODULE_TARGET}
            POST_BUILD
            COMMAND mkdir ${AndroidDAW_SDK_MODULE_NAME_ROOT}/${PREFIX_PARENT_AndroidDAW_SDK} || true
    )

    add_custom_command(
            TARGET ${AndroidDAW_SDK_MODULE_TARGET}
            POST_BUILD
            COMMAND mkdir ${AndroidDAW_SDK_LIBRARY_EXPORT_PREFIX} || true
    )
    add_custom_command(
            TARGET ${AndroidDAW_SDK_MODULE_TARGET}
            POST_BUILD
            COMMAND cp -v $<TARGET_FILE:${AndroidDAW_SDK_MODULE_TARGET}> ${AndroidDAW_SDK_LIBRARY_EXPORT_PREFIX}
    )
endmacro()