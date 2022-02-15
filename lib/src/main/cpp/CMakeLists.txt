cmake_minimum_required(VERSION 3.6)
project(rtmpdroid)

include(ExternalProject)
find_program(GIT "git")

set(OPENSSL_VERSION "openssl-3.0.1")
set(RTMP_VERSION "f1b83c10d8beb43fcc70a6e88cf4325499f25857")

# OpenSSL - needs few executable such as perl and mv in PATH
ExternalProject_Add(openssl_project
        GIT_REPOSITORY https://github.com/openssl/openssl.git
        GIT_TAG ${OPENSSL_VERSION}
        PATCH_COMMAND ${GIT} am ${CMAKE_CURRENT_SOURCE_DIR}/patches/0001-android-x86-target-Add-latomic-for-clang.patch
        CONFIGURE_COMMAND ${CMAKE_COMMAND} -E env PATH=${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN}/bin:$ENV{PATH} CC=${CMAKE_C_COMPILER} ANDROID_NDK_ROOT=${ANDROID_NDK} perl <SOURCE_DIR>/Configure android-${ANDROID_ARCH_NAME} -D__ANDROID_API__=${ANDROID_NATIVE_API_LEVEL} --openssldir=${CMAKE_LIBRARY_OUTPUT_DIRECTORY} --libdir="" --prefix=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        BUILD_COMMAND ${CMAKE_COMMAND} -E env PATH=${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN}/bin:$ENV{PATH} ANDROID_NDK_ROOT=${ANDROID_NDK} make
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.so ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.so
        BUILD_IN_SOURCE 1
        )

add_library(ssl SHARED IMPORTED)
add_dependencies(ssl openssl_project)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.so)

add_library(crypto SHARED IMPORTED)
add_dependencies(crypto openssl_project)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.so)

# RTMP
ExternalProject_Add(rtmp_project
        GIT_REPOSITORY http://git.ffmpeg.org/rtmpdump
        GIT_TAG ${RTMP_VERSION}
        PATCH_COMMAND ${GIT} am ${CMAKE_CURRENT_SOURCE_DIR}/patches/0001-Port-to-openssl-1.1.1.patch
            && ${GIT} am ${CMAKE_CURRENT_SOURCE_DIR}/patches/0002-Add-CMakeLists.txt.patch
            && ${GIT} am ${CMAKE_CURRENT_SOURCE_DIR}/patches/0003-Fix-AMF_EncodeString-size-check.patch
            && ${GIT} am ${CMAKE_CURRENT_SOURCE_DIR}/patches/0004-Modernize-socket-API-usage.patch
        CMAKE_ARGS
        -DENABLE_EXAMPLES=OFF
        -DOPENSSL_INCLUDE_DIR=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include
        -DOPENSSL_CRYPTO_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.so
        -DOPENSSL_SSL_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.so
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_PREFIX=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_LIBDIR=.
        -DCMAKE_INSTALL_INCLUDEDIR=include
        -DCMAKE_INSTALL_BINDIR=bin
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DANDROID_TOOLCHAIN=${ANDROID_TOOLCHAIN}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_STL=${ANDROID_STL}
        -DANDROID_PIE=${ANDROID_PIE}
        -DANDROID_CPP_FEATURES=${ANDROID_CPP_FEATURES}
        -DANDROID_ALLOW_UNDEFINED_SYMBOLS=${ANDROID_ALLOW_UNDEFINED_SYMBOLS}
        -DANDROID_ARM_MODE=${ANDROID_ARM_MODE}
        -DANDROID_ARM_NEON=${ANDROID_ARM_NEON}
        -DANDROID_DISABLE_FORMAT_STRING_CHECKS=${ANDROID_DISABLE_FORMAT_STRING_CHECKS}
        -DANDROID_CCACHE=${ANDROID_CCACHE}
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/librtmp.so
        DEPENDS crypto ssl
        BUILD_IN_SOURCE 1
        )

add_library(rtmp SHARED IMPORTED)
add_dependencies(rtmp rtmp_project)
set_target_properties(rtmp PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/librtmp.so)

# Target library
add_library(rtmpdroid SHARED glue.cpp)
include_directories(${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)
target_link_libraries(rtmpdroid log android rtmp)
