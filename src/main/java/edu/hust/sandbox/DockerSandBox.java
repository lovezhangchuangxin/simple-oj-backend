package edu.hust.sandbox;

public interface DockerSandBox {
    void pullImage(String image);

    String createContainer(String image, String containerName);
}
