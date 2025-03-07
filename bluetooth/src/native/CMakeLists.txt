cmake_minimum_required(VERSION 3.31)

project(hello_library)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")

############################################################
# Create a library
############################################################

#Generate the static library from the library sources
add_library(hello_library STATIC
    cpp/devices.cpp
    cpp/services.cpp
    cpp/spp.cpp
    cpp/utils.cpp
)

target_include_directories(hello_library
    PUBLIC
        ${PROJECT_SOURCE_DIR}/include
)
