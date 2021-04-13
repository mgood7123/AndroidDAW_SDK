cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK_Midi")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

include(CheckIncludeFiles)

include_directories(include)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../CMAKE_HELPER ${CMAKE_CURRENT_BINARY_DIR}/CMAKE_HELPER)
set_AndroidDAW_SDK_ROOT(${CMAKE_CURRENT_SOURCE_DIR}/../../../..)
set_module_name_AndroidDAW_SDK(Midi)
# ${AndroidDAW_SDK_MODULE_TARGET}

check_include_files(unistd.h HAVE_UNISTD_H)
check_include_files(humdrum.h HAVE_HUMDRUM_H)
check_include_files(sys/io.h HAVE_SYS_IO_H)

add_library(${AndroidDAW_SDK_MODULE_TARGET} SHARED
        src/Options.cpp
        src/Binasc.cpp
        src/MidiEvent.cpp
        src/MidiEventList.cpp
        src/MidiFile.cpp
        src/MidiMessage.cpp
)

export_library_AndroidDAW_SDK()
