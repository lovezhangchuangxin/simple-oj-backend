package edu.hust.sandbox;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface JudgerLibrary extends Library {
    JudgerLibrary INSTANCE = Native.load("libjudger.so", JudgerLibrary.class);

    void run(JudgerConfig.ByReference config, JudgeResult.ByReference result);
}