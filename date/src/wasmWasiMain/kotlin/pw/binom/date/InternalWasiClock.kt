package pw.binom.date

@WasmImport("wasi:clocks/monotonic-clock@0.2.5", "now")
internal external fun __wasm_import_now(): Long
