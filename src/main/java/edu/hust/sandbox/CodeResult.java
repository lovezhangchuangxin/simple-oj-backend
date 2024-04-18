package edu.hust.sandbox;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeResult {
    public int cpu_time;
    public int real_time;
    public long memory;
    public int signal;
    public int exit_code;
    public int error;
    public int result;
}
