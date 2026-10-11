package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class UploadController {

    private static final String UPLOAD_DIR = "E:\\Claude Code\\backend\\src\\main\\resources\\static\\uploads";

    @Value("${file.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping
    public Result<?> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 确保上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        // 保存文件
        File dest = new File(uploadDir, fileName);
        try {
            file.transferTo(dest);
            // 返回完整文件访问路径
            String fileUrl = baseUrl + "/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的头像");
        }

        File uploadDir = new File(UPLOAD_DIR + "/avatars");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = "avatar_" + System.currentTimeMillis() + suffix;

        File dest = new File(uploadDir, fileName);
        try {
            file.transferTo(dest);
            String fileUrl = baseUrl + "/uploads/avatars/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("头像上传失败");
        }
    }

    static class FileInfo {
        private String url;

        public FileInfo(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
