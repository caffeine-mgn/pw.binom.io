package pw.binom.compression.zlib

class Cursor {
    var inputOffset: Int = 0
    var inputLength: Int = 0
    var outputOffset: Int = 0
    var outputLength: Int = 0

    var availIn: Int
        get() = inputLength - inputOffset
        set(value) {
            inputOffset = inputLength - value
        }

    var availOut: Int
        get() = outputLength - outputOffset
        set(value) {
            outputOffset = outputLength - value
        }

    override fun toString(): String {
        return "Cursor(inputOffset=$inputOffset, inputLength=$inputLength, availIn=$availIn, outputOffset=$outputOffset, outputLength=$outputLength, availOut=$availOut)"
    }
}
