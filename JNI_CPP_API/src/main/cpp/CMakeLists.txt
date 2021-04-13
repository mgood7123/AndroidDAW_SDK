cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK_JNI_CPP_API")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

include_directories(include)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../CMAKE_HELPER ${CMAKE_CURRENT_BINARY_DIR}/CMAKE_HELPER)
set_AndroidDAW_SDK_ROOT(${CMAKE_CURRENT_SOURCE_DIR}/../../../..)
set_module_name_AndroidDAW_SDK(JNI_CPP_API)
# ${AndroidDAW_SDK_MODULE_TARGET}

import_library__AndroidDAW_JniHelpers()

add_library(${AndroidDAW_SDK_MODULE_TARGET} SHARED src/cpp_api.cpp)
target_link_libraries(${AndroidDAW_SDK_MODULE_TARGET} AndroidDAW_JniHelpers)

export_library_AndroidDAW_SDK()
