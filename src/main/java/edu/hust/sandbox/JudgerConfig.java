package edu.hust.sandbox;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class JudgerConfig extends Structure {
    public int max_cpu_time;
    public int max_real_time;
    public long max_memory;
    public long max_stack;
    public int max_process_number;
    public long max_output_size;
    public int memory_limit_check_only;
    public String exe_path;
    public String input_path;
    public String output_path;
    public String error_path;
    public String[] args = new String[100];
    public String[] env = new String[100];
    public String log_path;
    public String seccomp_rule_name;
    public int uid;
    public int gid;

    public static class ByReference extends JudgerConfig implements Structure.ByReference {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "max_cpu_time", "max_real_time", "max_memory", "max_stack",
                "max_process_number", "max_output_size", "memory_limit_check_only",
                "exe_path", "input_path", "output_path", "error_path",
                "args", "env", "log_path", "seccomp_rule_name", "uid", "gid"
        );
    }
}
