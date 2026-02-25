package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileController {

    // 上传路径，建议配置在 application.yml 中，这里简化硬编码
    // 注意：Windows 下路径分隔符问题，这里使用 e:/aidemo/uploads/
    private static final String UPLOAD_DIR = "e:/aidemo/uploads/";

    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;

        File dest = new File(UPLOAD_DIR + newFilename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
            // 返回可访问的 URL 路径
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}
