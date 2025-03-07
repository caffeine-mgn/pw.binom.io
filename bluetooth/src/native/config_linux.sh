#!/bin/sh
rm -Rf compile_commands.json
rm -Rf CMakeFiles
rm -f CMakeCache.txt
rm -f cmake_install.cmake
rm -Rf .cache
rm -Rf build
rm -f Makefile
cmake -DCMAKE_EXPORT_COMPILE_COMMANDS=ON .
