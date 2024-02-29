package edu.hust.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import edu.hust.constant.FileType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOSSUtils {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String imagesPath;
    private String codePath;
    private String problemPath;

    /**
     * 上传图片到OSS
     */
    public String upload(MultipartFile file, FileType fileType) throws Exception {
        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String fileName = getFilePath(fileType) + UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        // 文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        return url;
    }

    /**
     * 根据 fileType 获取文件路径
     */
    public String getFilePath(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> imagesPath;
            case CODE -> codePath;
            case PROBLEM -> problemPath;
            default -> null;
        };
    }
}
