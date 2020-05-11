# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#include(ndk-stl-config.cmake)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# set binary output folder to Android assets folder
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "src/main/assets/${ANDROID_ABI}")
#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "../src/main/lib/assets/${ANDROID_ABI}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "../../../../src/main/assets/lib/${ANDROID_ABI}")
#set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "../src/main/bin/assets/${ANDROID_ABI}")

#set( lib_build_DIR ../gmath )
#file(MAKE_DIRECTORY ${lib_build_DIR})

add_library(
        dl-lib
        SHARED
        IMPORTED
)

set_target_properties( # Specifies the target library.
        dl-lib

        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION

        # Provides the path to the library you want to import.
        ${CMAKE_SOURCE_DIR}/lib/${ANDROID_ABI}/libdl.so )

include_directories("lib/include")

add_library(
        # Sets the name of the library.
        rpxhook

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).

        #dlext_namespaces.h

        native-lib.cpp
        rp_backtrace.cpp
        rp_pointer_manager.cpp
        rp_nativehook_sensors.c
        rp_nativehook_audio.c
        rp_nativehook_camera.c
        rp_camera_hook_manager.cpp
        rp_nativehook_dlopen.c
        rp_xhook_jni.c
        xhook.c
        xh_core.c
        xh_elf.c
        xh_jni.c
        xh_log.c
        xh_util.c
        xh_version.c
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        # Specifies the target library.
        rpxhook
        android

        
        #aaudio #No supported on older version of Android
        OpenSLES
        #camera2ndk
        dl-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
)

#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
#set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# target_include_directories (rpxhook PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})