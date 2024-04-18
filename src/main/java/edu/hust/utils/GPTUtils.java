package edu.hust.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class GPTUtils {
    /**
     * 调用 GPT 接口
     */
    public static String callFreeGPT_1(String content) throws IOException {
        // POST: https://api.chatanywhere.com.cn/v1/chat/completions
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost("https://api.chatanywhere.com.cn/v1/chat/completions");
        // 设置请求头
        request.setHeader("Content-Type", "application/json");
        // 记得别暴露 API Key
        request.setHeader("Authorization", "your api key");
        // 设置请求体
        // request.setEntity(new StringEntity("{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"" + content + "\"}],\"temperature\": 0.7}", "UTF-8"));
        request.setEntity(new StringEntity(content, "UTF-8"));
        // 发送请求
        CloseableHttpResponse response = httpClient.execute(request);
        // 获取响应
        String result;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            result = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        // 关闭连接
        response.close();
        return result;
    }
}
