package com.github.aakumykov.simple_audio_recorder.utils;

public final class ArgumentChecker {

    private ArgumentChecker() {}

    public static void checkNotNull(Object filePath) {
        if (null == filePath)
            throw new IllegalArgumentException("Argument cannot be null");
    }
}
