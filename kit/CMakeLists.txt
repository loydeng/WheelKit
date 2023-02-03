
cmake_minimum_required(VERSION 3.22.1)

project(kit)

set(SOURCE_DIR src/main/cpp)

include_directories(SYSTEM ${SOURCE_DIR}/include)

add_library(ffmpeg SHARED IMPORTED)

set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/ffmpeg/${ANDROID_ABI}/libffmpeg.so)

aux_source_directory(${SOURCE_DIR} CPP_FILES)
message(${CPP_FILES})
add_library(kit SHARED ${CPP_FILES})

target_link_libraries(
        kit
        ffmpeg
        OpenSLES
        log)