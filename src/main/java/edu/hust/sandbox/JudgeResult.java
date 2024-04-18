package edu.hust.sandbox;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class JudgeResult extends Structure {
    public int cpu_time;
    public int real_time;
    public long memory;
    public int signal;
    public int exit_code;
    public int error;
    public int result;

    public static class ByReference extends JudgeResult implements Structure.ByReference {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "cpu_time", "real_time", "memory", "signal", "exit_code", "error", "result"
        );
    }
}
