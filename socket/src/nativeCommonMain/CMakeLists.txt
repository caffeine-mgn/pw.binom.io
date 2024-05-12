cmake_minimum_required(VERSION 3.22)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
project(native C)
set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_C_STANDARD 11)

set(CMAKE_C_COMPILER clang-cl)
set(CMAKE_CXX_COMPILER clang-cl)
set(CMAKE_AR llvm-lib)
set(CMAKE_LINKER lld-link)
set(CMAKE_MT llvm-mt)

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
