cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK__Plugin")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

include_directories(include)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../CMAKE_HELPER ${CMAKE_CURRENT_BINARY_DIR}/CMAKE_HELPER)
set_AndroidDAW_SDK_ROOT(${CMAKE_CURRENT_SOURCE_DIR}/../../../..)
set_module_name_AndroidDAW_SDK(Plugin)
# ${AndroidDAW_SDK_MODULE_TARGET}

add_library(${AndroidDAW_SDK_MODULE_TARGET} SHARED TempoGrid.cpp)

AndroidDAW_SDK_CMAKE_HELPER_import_library(Midi)

target_link_libraries(${AndroidDAW_SDK_MODULE_TARGET} AndroidDAW_Midi)

export_library_AndroidDAW_SDK()
