cmake_minimum_required(VERSION 3.22.1)

project("executorch-qnn")

# Define QNN SDK Path
set(QNN_SDK_ROOT "C:/Users/rawat/Downloads/v2.28.0.241029/qairt/2.28.0.241029")

# Include directories for QNN
include_directories(${QNN_SDK_ROOT}/include/qnn)

# Find Android system libraries
find_library(log-lib log)
find_library(android-lib android)

# Debug: Print paths
message(STATUS "CMAKE_SOURCE_DIR: ${CMAKE_SOURCE_DIR}")
message(STATUS "ANDROID_ABI: ${ANDROID_ABI}")
set(QNN_LIB_PATH "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")
message(STATUS "QNN_LIB_PATH: ${QNN_LIB_PATH}")

# Import QNN libraries
add_library(qnn_htp SHARED IMPORTED)
set_target_properties(qnn_htp PROPERTIES IMPORTED_LOCATION "${QNN_LIB_PATH}/libQnnHtp.so")

add_library(qnn_system SHARED IMPORTED)
set_target_properties(qnn_system PROPERTIES IMPORTED_LOCATION "${QNN_LIB_PATH}/libQnnSystem.so")

# Define our JNI library
add_library(executorch_jni SHARED
    executorch_jni.cpp
)

# Link libraries
target_link_libraries(executorch_jni
    ${log-lib}
    ${android-lib}
    qnn_htp
    qnn_system
)
