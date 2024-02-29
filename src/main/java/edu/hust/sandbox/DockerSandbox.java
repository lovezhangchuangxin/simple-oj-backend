package edu.hust.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
public class DockerSandbox {
    private final DockerUtils dockerUtils;

    public DockerSandbox(DockerUtils dockerUtils) {
        this.dockerUtils = dockerUtils;
    }

    /**
     * 创建容器，编译并执行代码
     */
    public List<CodeExecuteResult> executeCode(String codePath, String language, Integer sampleSize, Integer timeLimit, Integer memoryLimit) {
        // 选择镜像
        String image = getImage(language);
        if (image == null) {
            log.error("不支持的语言");
            return null;
        }

        HostConfig hostConfig = new HostConfig();
        // 内存限制
        hostConfig.withMemory(memoryLimit * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        // CPU 限制
        hostConfig.withCpuCount(1L);
        // seccomp
        hostConfig.withSecurityOpts(List.of("seccomp=unconfined"));
        // 文件映射
        hostConfig.setBinds(new Bind(codePath, new Volume("/app")));
        // 创建容器
        String containerName = language + "-" + UUID.randomUUID();
        CreateContainerResponse response = dockerUtils.createContainer(image, containerName, hostConfig);
        String containerId = response.getId();
        // 运行容器
        dockerUtils.startContainer(containerId);

        // 编译代码
        String compileCommand = getCompileCommand(language);
        if (compileCommand != null) {
            try {
                dockerUtils.createAndExecuteCmd(containerId, compileCommand.split(" "), 2);
                Thread.sleep(500);
                dockerUtils.createAndExecuteCmd(containerId, "cd /app".split(" "), 1);
                // 等待，确保编译完成
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // 关闭容器
                dockerUtils.stopAndRemoveContainer(containerId);
                throw new RuntimeException(e);
            }
        }

        DockerClient dockerClient = dockerUtils.getDockerClient();
        List<CodeExecuteResult> executeResultList = new ArrayList<>();

        if (sampleSize == 0) {
            dockerUtils.stopAndRemoveContainer(containerId);
            return executeResultList;
        }

        for (int i = 1; i <= sampleSize; i++) {
            String cmd = getExecuteCommand(language);
            // 命令行参数上拼上输入文件
            cmd = cmd + " " + "/app/" + i + ".in";

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmd.split(" "))
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();

            // 执行结果对象
            CodeExecuteResult executeResult = new CodeExecuteResult();
            // 是否超时
            final boolean[] timeout = {true};
            // 最大内存占用
            final long[] maxMemory = {0};

            // 执行结果回调
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame item) {
                    StreamType streamType = item.getStreamType();
                    if (streamType == StreamType.STDOUT) {
                        executeResult.setOutput(new String(item.getPayload()));
                    } else if (streamType == StreamType.STDERR) {
                        executeResult.setError(new String(item.getPayload()));
                    }
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                    timeout[0] = false;
                }
            };

            // 获取最大内存占用
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {
                }

                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                }
            });

            statsCmd.exec(statisticsResultCallback);

            try {
                Long start = System.currentTimeMillis();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(1000, TimeUnit.MILLISECONDS);
                Long end = System.currentTimeMillis();
                executeResult.setTime((short) (end - start));
                statsCmd.close();
            } catch (Exception e) {
                dockerUtils.stopAndRemoveContainer(containerId);
                throw new RuntimeException(e);
            }

            executeResult.setTimeout(timeout[0]);
            executeResult.setMemory((short) (maxMemory[0] / 1024 / 1024));
            executeResultList.add(executeResult);
        }

        dockerUtils.stopAndRemoveContainer(containerId);
        return executeResultList;
    }

    private String getExecuteCommand(String language) {
        return switch (language) {
            case "java" -> "java -cp /app Main";
            case "c", "cpp" -> "/app/main";
            case "python" -> "python /app/main.py";
            case "node" -> "node /app/main.js";
            default -> "";
        };
    }

    private String getCompileCommand(String language) {
        return switch (language) {
            case "java" -> "javac -cp /app /app/Main.java";
            case "c" -> "gcc -o /app/main /app/main.c";
            case "cpp" -> "g++ -o /app/main /app/main.cpp";
            default -> null;
        };
    }

    /**
     * 根据语言选择镜像
     */
    private String getImage(String language) {
        return switch (language) {
            case "java" -> Constant.JAVA_IMAGE;
            case "c" -> Constant.C_IMAGE;
            case "cpp" -> Constant.CPP_IMAGE;
            case "python" -> Constant.PYTHON_IMAGE;
            case "node" -> Constant.NODE_IMAGE;
            default -> null;
        };
    }

    /**
     * 处理用户代码，将输入重定向
     */
    public String redirectInput(String input) {
        // c 语言中在 main 函数开头添加输入重定向
        return input.replaceAll("(int\\s+main.+\\{)", "$1\n    freopen(argv[1], \"r\", stdin);\n");
    }
}
