#!/bin/sh
rm -Rf compile_commands.json
rm -Rf CMakeFiles
rm -Rf .cache
rm -Rf build
cmake -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -DCMAKE_TOOLCHAIN_FILE=toolchain.cmake .
