package edu.hust;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import edu.hust.sandbox.DockerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class DockerUtilsTests {
    @Autowired
    private DockerUtils dockerUtils;

    @Test
    public void testDockerJava() {
        Info info = dockerUtils.getDockerClient().infoCmd().exec();
        System.out.println("docker info : " + info.toString());
    }

    @Test
    public void testInspectImage() {
        String image = "welcome-to-docker:latest";
        dockerUtils.inspectImage(image);
    }

    @Test
    public void testPullImage() {
        // String image = "openjdk:8-alpine";
        String image = "gcc:latest";
        dockerUtils.pullImage(image);
    }

    @Test
    public void testRemoveImage() {
        String image = "nginx:latest";
        dockerUtils.removeImage(image);
    }

    @Test
    public void testImageList() {
        List<Image> images = dockerUtils.imageList();
        images.forEach(image -> System.out.println(image.toString()));
    }

    @Test
    public void testCreateContainer() {
        String image = "docker/welcome-to-docker:latest";
        String containerName = "welcome-to-docker-" + UUID.randomUUID();
        HostConfig hostConfig = new HostConfig();
        // 内存限制
        hostConfig.withMemory(100 * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        // CPU 限制
        hostConfig.withCpuCount(1L);

        CreateContainerResponse response = dockerUtils.createContainer(image, containerName, hostConfig);
        System.out.println(response.getId());
    }

    @Test
    public void testStartContainer() {
        String containerId = "6f36f33b36d1177664ed0806c401c8eb89c9376a5b8f6a9ab156c3a8189a6469";
        dockerUtils.startContainer(containerId);
    }

    @Test
    public void testStopContainer() {
        String containerId = "6f36f33b36d1177664ed0806c401c8eb89c9376a5b8f6a9ab156c3a8189a6469";
        dockerUtils.stopContainer(containerId);
    }

    @Test
    public void testRemoveContainer() {
        String containerId = "daaeca0564765465f4457fcb5139f61abb2cabba103a0ecbb976d702b52aa562";
        dockerUtils.stopAndRemoveContainer(containerId);
    }
}
