package edu.hust.sandbox;

public class Judger {
    public static void run(JudgerConfig.ByReference config, JudgeResult.ByReference result) {
        JudgerLibrary.INSTANCE.run(config, result);
    }
}
