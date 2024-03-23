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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public void multiUpload(String[] names,
                            String[] md5s,
                            MultipartFile[] files) throws IOException {
        for (int i = 0; i < files.length; i++) {
            fileService.upload(names[i], md5s[i], files[i]);
    public ResponseEntity<String> multiUpload(MultipartFile[] files) {
        List<String> errorsizeFiles = new ArrayList<>();
        List<String> dupFiles = new ArrayList<>();

        long totalFileSize = 0;

        try {
            for (MultipartFile file : files) {
                try {
                    fileUtils.checkFileSize(file, maxFileSize);
                    totalFileSize += file.getSize();
                    FileDTO fileDTO = convertToDTO(file);
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


    private FileDTO convertToDTO(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        FileDTO fileDTO = new FileDTO();
        String path = UploadConfig.path + file.getOriginalFilename();
        fileDTO.setName(file.getOriginalFilename());
        fileDTO.setPath(path);
        fileDTO.setMd5(FileUtils.write(path, file.getInputStream()));
        fileDTO.setUploadTime(new Date());
        fileDTO.setExtractKeysData("");
        return fileDTO;
    }
}
