cmake_minimum_required(VERSION 3.22)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
project(native C)
set(CMAKE_C_STANDARD 11)
#set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_C_COMPILER clang-cl)
set(CMAKE_CXX_COMPILER clang-cl)
set(CMAKE_AR llvm-lib)
set(CMAKE_LINKER lld-link)
set(CMAKE_MT llvm-mt)
#include_directories("/home/subochev/.konan/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32/include")
#include_directories("/home/subochev/.konan/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot/usr/include/")
include_directories("/usr/include")
add_library(
        native
        include/NSelector.h
        include/wepoll.h
        include/definition.h
        include/NSelectedList.h
        include/NInetSocketNetworkAddress.h
        include/NSocket.h
        include/NNetworkAddress.h
        include/Network.h
        include/all.h
        src/NSelector.c
        src/NSelectedList.c include/NEvent.h
        src/NEvent.c
        src/wepoll.c
        src/NInetSocketNetworkAddress.c
        src/NSocket.c
        src/err.c
        src/NNetworkInterface.c
        src/NNetworkAddress.c
        src/Network.c
)
#set(CMAKE_SYSTEM_NAME Windows)
#add_definitions(-DWIN32)
#add_compile_definitions(WINDOWS_TARGET WIN32)
#target_compile_definitions(native PRIVATE WINDOWS_TARGET=1)