package edu.hust.utils;

import com.google.gson.Gson;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.sandbox.CodeResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class JudgerUtils {
    /**
     * 执行代码
     */
    public static CodeResult execute(String language, String exePath, String inputPath, String outputPath, String errorPath, int timeLimit, int memoryLimit) {
        String command = "./libjudger.so" + " --exe_path=" + exePath + " --input_path=" + inputPath + " --output_path=" + outputPath + " --error_path=" + errorPath + " --max_cpu_time=" + timeLimit + " --max_real_time=" + (timeLimit * 2) + " --max_memory=" + memoryLimit + " --max_stack=33554432" + " --max_output_size=102400";
        try {
            Process process = Runtime.getRuntime().exec(command);
            // 获取输出
            String result;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                result = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
            process.waitFor();
            return parseResult(result);
        } catch (Exception e) {
            throw new HustOjException(ExceptionCodeEnum.EXECUTE_ERROR);
        }
    }

    /**
     * 编译代码
     */
    public static void compile(String language, String srcPath, String exePath) {
        switch (language) {
            case "c":
                compileC(srcPath, exePath);
                break;
            case "cpp":
                compileCpp(srcPath, exePath);
                break;
            default:
                throw new HustOjException(ExceptionCodeEnum.COMPILE_ERROR);
        }
    }

    /**
     * 编译 C 代码
     */
    public static void compileC(String srcPath, String exePath) {
        String command = "gcc " + srcPath + " -o " + exePath + " -O2 -w -lm --static";
        try {
            Process process = Runtime.getRuntime().exec(command);
            // 编译错误信息
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String error = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                if (!error.isEmpty()) {
                    throw new HustOjException(ExceptionCodeEnum.COMPILE_ERROR, error);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            throw new HustOjException(ExceptionCodeEnum.COMPILE_ERROR, e.getMessage());
        }
    }

    /**
     * 编译 C++ 代码
     */
    public static void compileCpp(String srcPath, String exePath) {
        String command = "g++ " + srcPath + " -o " + exePath + " -O2 -w -lm --static -std=c++11";
        try {
            Process process = Runtime.getRuntime().exec(command);
            // 编译错误信息
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String error = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                if (!error.isEmpty()) {
                    throw new HustOjException(ExceptionCodeEnum.COMPILE_ERROR, error);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            throw new HustOjException(ExceptionCodeEnum.COMPILE_ERROR, e.getMessage());
        }
    }

    /**
     * 解析结果
     * 结果格式：
     * {
     * "cpu_time": 0,
     * "real_time": 0,
     * "memory": 860160,
     * "signal": 25,
     * "exit_code": 0,
     * "error": 0,
     * "result": 4
     * }
     */
    public static CodeResult parseResult(String result) {
        return new Gson().fromJson(result, CodeResult.class);
    }
}