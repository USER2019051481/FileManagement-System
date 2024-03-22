package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/File")
@CrossOrigin
public class FileUploadController {
    @Autowired
    private FileService fileService;

    @PostMapping("/")
    public ResponseEntity multiUpload(String[] names,
                                      String[] md5s,
                                      MultipartFile[] files) throws IOException {
        for (int i = 0; i < files.length; i++) {
            fileService.upload(names[i], md5s[i], files[i]);
        }
        return ResponseEntity.ok(200) ;
    }
}
