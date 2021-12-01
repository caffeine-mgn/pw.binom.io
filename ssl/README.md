# Binom SSL
Kotlin Library for SSL

## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:ssl:<version>"
}
```


## Build OpenSSL
### Windows
##MSYS
Install MSYS. You can execute `winget install msys2`.
Default install path is `C:\msys64`.
Also install make and perl using cmd `C:\msys64\usr\bin\pacman -S perl make`

## Building
#Preparing
```
set MSYS=C:\msys64
set OPENSSL=C:\TEMP\openssl-1.1.1i
set PATH=%PATH%;%MSYS%\usr\bin;%userprofile%\.konan\dependencies\msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1\bin
set CC=clang
set CXX=clang
set AR=llvm-ar
set ARFLAGS=rc
cd %OPENSSL%
```

#Build X64
```
rmdir /s /q tmp
make clean
perl.exe Configure mingw64 no-zlib no-zlib-dynamic no-shared no-threads "--target=x86_64-w64-mingw32 -O3 \"--sysroot=%userprofile%/.konan/dependencies/msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1\""
make.exe build_libs -j 16
mkdir tmp
cd tmp
ar -x ../libssl.a
ar -x ../libcrypto.a
ar -rv libopenssl.a *.o
cp libopenssl.a %OPENSSL%/libopenssl.a
cd %OPENSSL%
rmdir /s /q tmp
```
#Build X86
```
make clean
perl.exe Configure mingw no-shared no-threads "--target=i686-w64-mingw32 -O3 \"--sysroot=%userprofile%\.konan\dependencies\msys2-mingw-w64-i686-clang-llvm-lld-compiler_rt-8.0.1\""
make.exe build_libs -j 16
mkdir tmp
cd tmp
ar -x ../libssl.a
ar -x ../libcrypto.a
ar -rv libopenssl.a *.o
cp libopenssl.a %OPENSSL%/libopenssl.a
cd %OPENSSL%
rmdir /s /q tmp
```

#Build Linux X64
```
make clean
perl.exe Configure linux-x86_64 no-shared no-threads "--target=x86_64-unknown-linux-gnu -O3 \"--sysroot=%userprofile%/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64/x86_64-unknown-linux-gnu/sysroot\""
make.exe build_libs -j 16
mkdir tmp
cd tmp
ar -x ../libssl.a
ar -x ../libcrypto.a
ar -rv libopenssl.a *.o
```