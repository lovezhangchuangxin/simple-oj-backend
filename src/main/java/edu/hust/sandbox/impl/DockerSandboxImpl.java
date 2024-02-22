package edu.hust.sandbox.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import edu.hust.sandbox.DockerSandBox;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class DockerSandboxImpl implements DockerSandBox {
    private final DockerClient dockerClient;

    public DockerSandboxImpl() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    /**
     * 拉取镜像
     */
    @Override
    public void pullImage(String image) {
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("拉取镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            dockerClient.pullImageCmd(image).exec(pullImageResultCallback).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("拉取镜像失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建容器
     */
    public String createContainer(String image, String containerName) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
//        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        return createContainerResponse.getId();
    }
}
