cmake_minimum_required(VERSION 3.22)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
project(native VERSION 1.0 LANGUAGES C)
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