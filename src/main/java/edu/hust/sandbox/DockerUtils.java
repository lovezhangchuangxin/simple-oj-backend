package edu.hust.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
public class DockerUtils {
    private final DockerClient dockerClient;

    public DockerUtils(@Value("${docker.host}") String host, @Value("${docker.api-version}") String apiVersion) {
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(host)
                .withApiVersion(apiVersion) // docker version 查看
                .build();

        // 创建 DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * 拉取镜像
     */
    public void pullImage(String image) {
        try {
            dockerClient.pullImageCmd(image)
                    .start()
                    .awaitCompletion(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看镜像详细信息
     */
    public void inspectImage(String image) {
        InspectImageResponse response = dockerClient.inspectImageCmd(image).exec();
        System.out.println(new Gson().toJson(response));
    }

    /**
     * 删除镜像
     */
    public void removeImage(String image) {
        dockerClient.removeImageCmd(image).exec();
    }

    /**
     * 获取镜像列表
     */
    public List<Image> imageList() {
        return dockerClient.listImagesCmd().withShowAll(true).exec();
    }

    /**
     * 创建容器
     */
    public CreateContainerResponse createContainer(String image, String containerName, HostConfig hostConfig) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image)
                // 容器名称
                .withName(containerName)
                // 主机配置
                .withHostConfig(hostConfig)
                // 关闭网络
                .withNetworkDisabled(true)
                // 开启标准输入输出
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withStdinOpen(true)
                // 开启 tty
                .withTty(true);

        return containerCmd.exec();
    }

    /**
     * 启动容器
     */
    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * 停止容器
     */
    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /**
     * 删除容器
     */
    public void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId).exec();
    }

    /**
     * 停止并删除容器
     */
    public void stopAndRemoveContainer(String containerId) {
        stopContainer(containerId);
        removeContainer(containerId);
    }

    /**
     * 容器中创建命令
     */
    public String createCmd(String containerId, String[] cmd) {
        return dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .exec()
                .getId();
    }

    /**
     * 容器中执行命令
     */
    public void executeCmd(String cmdId, Integer timeout) throws InterruptedException {
        dockerClient.execStartCmd(cmdId)
                .start()
                .awaitCompletion(timeout, TimeUnit.SECONDS);
    }

    /**
     * 容器中创建并执行命令
     */
    public void createAndExecuteCmd(String containerId, String[] cmd, Integer timeout) throws InterruptedException {
        String cmdId = createCmd(containerId, cmd);
        executeCmd(cmdId, timeout);
    }
}
