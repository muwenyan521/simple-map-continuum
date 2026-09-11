package com.muwenyan.simplemap.core.codec;

public sealed interface CoreError permits CoreError.InvalidInput, CoreError.UnsupportedFormat, CoreError.CorruptData {
    record InvalidInput(String message) implements CoreError { }
    record UnsupportedFormat(String format) implements CoreError { }
    record CorruptData(String message) implements CoreError { }
}
