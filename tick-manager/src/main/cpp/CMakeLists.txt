# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -Wall -UNDEBUG")

add_library(sync-manager-jni
            SHARED
        TickManager.cpp DeltaTickBuffer.cpp)

target_link_libraries(sync-manager-jni
                        log )
