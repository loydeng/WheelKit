
cmake_minimum_required(VERSION 3.22.1)

project(kit)

set(SOURCE_DIR src/main/cpp)

include_directories(${SOURCE_DIR}/include) #SYSTEM
include_directories(${SOURCE_DIR}/include/ffmpeg)
include_directories(${SOURCE_DIR}/include/gif)
include_directories(${SOURCE_DIR}/include/jni)

add_library(ffmpeg SHARED IMPORTED)

set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/ffmpeg/${ANDROID_ABI}/libffmpeg.so)

add_library(webrtc STATIC IMPORTED)

set_target_properties(webrtc PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/webrtc/${ANDROID_ABI}/libwebrtc${CMAKE_STATIC_LIBRARY_SUFFIX})

#file(GLOB_RECURSE CPP_FILES  ${SOURCE_DIR}/*.cpp)
aux_source_directory(${SOURCE_DIR} SRC_FILES)
aux_source_directory(${SOURCE_DIR}/jni JNI_FILES)
aux_source_directory(${SOURCE_DIR}/ffmpeg FFMPEG_CPP)
aux_source_directory(${SOURCE_DIR}/sl SL_CPP)
aux_source_directory(${SOURCE_DIR}/gl GL_CPP)
list(APPEND CPP_FILES ${SRC_FILES} ${JNI_FILES} ${FFMPEG_CPP} ${SL_CPP} ${GL_CPP})

add_library(kit SHARED ${CPP_FILES})

target_link_libraries(
        kit
        ffmpeg
        OpenSLES
        log)