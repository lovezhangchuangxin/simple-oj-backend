package edu.hust;

import edu.hust.sandbox.CodeExecuteResult;
import edu.hust.sandbox.DockerSandbox;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DockerSandBoxTests {
    @Autowired
    private DockerSandbox dockerSandbox;

    @Test
    public void testExecuteCode() {
        String codePath = "D:\\CollegeLife\\advancement\\Java\\tests";
        String language = "c";
        List<CodeExecuteResult> codeExecuteResults = dockerSandbox.executeCode(codePath, language, 1, 1000, 256);
        codeExecuteResults.forEach(codeExecuteResult -> System.out.println(codeExecuteResult.toString()));
    }

    @Test
    public void testTransformCode() {
        String code = "#include <stdio.h>\n" +
                "\n" +
                "int main(char *args[]) {\n" +
                "    int a, b;\n" +
                "    scanf(\"%d %d\", &a, &b);\n" +
                "    printf(\"%d\\n\", a + b);\n" +
                "    return 0;\n" +
                "}";
        System.out.println(dockerSandbox.redirectInput(code));
    }
}
