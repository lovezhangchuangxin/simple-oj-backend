package edu.hust;

import edu.hust.sandbox.DockerSandBox;
import edu.hust.sandbox.impl.DockerSandboxImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SandBoxTests {

    @Test
    public void testPullImage() {
        DockerSandBox dockerSandbox = new DockerSandboxImpl();
//        String image = "welcome-to-docker";
//        dockerSandbox.pullImage(image);
    }

    @Test
    public void test() {
        System.out.println("test");
    }
}
