package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.dto.FileDTO;

import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.service.Mapper.FileMapper;
import cn.attackme.myuploader.utils.HFileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.exception.FileSizeExceededException;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/File")
@CrossOrigin
@Slf4j
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @Autowired
    private HFileUtils fileUtils;



    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;

    @PostMapping("/Upload")
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

    /**
     *  判断上传文件与已有文件是否存在冲突
     * @param files 上传文件
     * @return 冲突内容
     * @throws IOException
     */
        @GetMapping(value = "/conflicts", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> getFileConflicts(@RequestHeader MultipartFile[] files) throws IOException {


        // 存储冲突行的列表
        List<String> conflictLines = new ArrayList<>() ;
        // 调用FileService获取冲突信息
       boolean hasConflict = fileService.isConflict(files,conflictLines) ;

        String jsonString = JSONObject.toJSONString(conflictLines);
        if(hasConflict){
            return ResponseEntity.ok(Collections.singletonList(jsonString));
        }else{
            return ResponseEntity.notFound().build() ;
        }

    }

}
