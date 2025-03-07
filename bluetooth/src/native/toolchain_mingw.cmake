# Указываем целевую систему
set(CMAKE_SYSTEM_NAME Windows)

# Указываем архитектуру процессора
set(CMAKE_SYSTEM_PROCESSOR x86_64)

# Указываем компиляторы Clang
set(CMAKE_C_COMPILER $ENV{HOME}/.konan/dependencies/llvm-16.0.0-x86_64-linux-essentials-80/bin/clang)
set(CMAKE_CXX_COMPILER $ENV{HOME}/.konan/dependencies/llvm-16.0.0-x86_64-linux-essentials-80/bin/clang++)

# Указываем целевой triple для кросс-компиляции (Windows, x86_64)
set(CMAKE_C_COMPILER_TARGET x86_64-w64-mingw32)
set(CMAKE_CXX_COMPILER_TARGET x86_64-w64-mingw32)

# Указываем путь к sysroot с использованием переменной среды
set(WINDOWS_SYSROOT $ENV{HOME}/.konan/dependencies/msys2-mingw-w64-x86_64-2)
set(CMAKE_SYSROOT ${WINDOWS_SYSROOT})

# Настраиваем поиск библиотек и заголовочных файлов
set(CMAKE_FIND_ROOT_PATH ${CMAKE_SYSROOT})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

# Указываем дополнительные флаги для Clang
set(CMAKE_C_FLAGS "--sysroot=${CMAKE_SYSROOT} -target x86_64-w64-mingw32")
set(CMAKE_CXX_FLAGS "--sysroot=${CMAKE_SYSROOT} -target x86_64-w64-mingw32")

# Указываем линкер (опционально, если требуется)
set(CMAKE_EXE_LINKER_FLAGS "-fuse-ld=lld")
