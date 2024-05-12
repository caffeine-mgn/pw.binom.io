cmake_minimum_required(VERSION 3.22)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
project(native C)
#set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_C_STANDARD 11)

set(CMAKE_C_COMPILER clang-cl)
set(CMAKE_CXX_COMPILER clang-cl)
set(CMAKE_AR llvm-lib)
set(CMAKE_LINKER lld-link)
set(CMAKE_MT llvm-mt)
#include_directories("/home/subochev/.konan/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32/include")
add_library(
        native
        src/NSelector.c
        include/NSelector.h
        include/wepoll.h
        include/definition.h
        include/NSelectedList.h
        src/NSelectedList.c include/NEvent.h
        src/NEvent.c
        src/wepoll.c
        include/NSocket.h
        include/NInetSocketNetworkAddress.h
        src/NInetSocketNetworkAddress.c
        src/NSocket.c
        src/err.c
        src/NNetworkInterface.c
        include/NNetworkAddress.h
        src/NNetworkAddress.c
        include/Network.h
        src/Network.c
        include/all.h
)
add_definitions(-DWIN32)
add_compile_definitions(WINDOWS_TARGET WIN32)
target_compile_definitions(native PRIVATE WINDOWS_TARGET=1)