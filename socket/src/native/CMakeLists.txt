cmake_minimum_required(VERSION 3.22)
project(native C)

set(CMAKE_C_STANDARD 11)
#set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

add_library(native src/Selector.c include/Selector.h include/wepoll.h include/definition.h include/SelectedList.h src/SelectedList.c include/Event.h src/Event.c src/wepoll.c)
