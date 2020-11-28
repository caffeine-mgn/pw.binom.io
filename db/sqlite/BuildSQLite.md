# Build
Current version of SQLite is `3.32.3`<br>
SQLite must static compiled for each supported platforms

**Build object file**<br>
`gcc -c sqlite3.c -o sqlite3.o -DSQLITE_THREADSAFE=1 -DSQLITE_ENABLE_FTS4 -DSQLITE_ENABLE_FTS5 -DSQLITE_ENABLE_JSON1  -DSQLITE_ENABLE_RTREE -DSQLITE_ENABLE_EXPLAIN_COMMENTS`

**Build static library from object file**<br>
`ar rcs libsqlite3.a sqlite3.o`

After build you must put result static library to each target direction:
* src/linuxX64Main/cinterop/lib
* src/linuxArm32HfpMain/cinterop/lib
* src/mingwX64Main/cinterop/lib
* src/mingwX86Main/cinterop/lib