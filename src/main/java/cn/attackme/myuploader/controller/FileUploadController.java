package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.utils.FileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.exception.FileSizeExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/File")
@CrossOrigin
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @Autowired
    private FileUtils fileUtils;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;

    @PostMapping("/")
    public ResponseEntity<String> multiUpload(@RequestHeader MultipartFile[] files) {
        // 用于存储文件大小超过限制的文件名
        List<String> errorsizeFiles = new ArrayList<>();
        // 用于存储已存在或文件名重复的文件名
        List<String> dupFiles = new ArrayList<>();

        long totalFileSize = 0;

        try {
            for (MultipartFile file : files) {
                try {
                    fileUtils.checkFileSize(file, maxFileSize);
                    totalFileSize += file.getSize();
                    FileDTO fileDTO = fileService.convertToDTO(file);

                    fileService.upload(fileDTO);
                } catch (FileSizeExceededException e) {
                    errorsizeFiles.add(file.getOriginalFilename());
                } catch (FileDuplicateException e) {
                    dupFiles.add(e.getMessage());
                }
            }

            if (totalFileSize > maxRequestSize.toBytes()) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("上传文件总大小超过最大限制");
            }

            if (!errorsizeFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("以下文件上传失败: " + errorsizeFiles);
            }

            if (!dupFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("以下文件已存在或文件名重复: " + dupFiles);
            }

            return ResponseEntity.ok("所有文件上传成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传失败");
        }
    }


}
