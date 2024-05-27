cmake_minimum_required(VERSION 3.22)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
project(native VERSION 1.0 LANGUAGES C)
add_library(
        native
        include/NWatcher.h
        src/NWatcher.c
)
#include_directories("/usr/include")
#set_target_properties(native PROPERTIES LINKER_LANGUAGE CXX)
#set(CMAKE_SYSTEM_NAME Windows)
#add_definitions(-DWIN32)
#add_compile_definitions(WINDOWS_TARGET WIN32)
#target_compile_definitions(native PRIVATE WINDOWS_TARGET=1)
