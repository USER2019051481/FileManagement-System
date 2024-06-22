package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;

import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.service.ValidateService;
import cn.attackme.myuploader.utils.HFileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/File")
@CrossOrigin
@Slf4j
@Api(tags = "Upload and Conflict File", description = "上传文件、冲突判断")
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @Autowired
    private HFileUtils fileUtils;
    @Autowired
    private ValidateService validateService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;


    @ResponseBody
    @PostMapping("/Upload")
    @ApiOperation(value = "文件上传", notes = "实现多文件上传，控制总文件大小<100000MB，同时判断文件重命名和内容重复情况")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> multiUpload( @RequestParam("files") MultipartFile[] files) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("身份未认证");
        }
        String hospitalName = (String) authentication.getPrincipal();
        try {
            String fileMessage = fileService.upload(files, hospitalName);
            if (fileMessage.equals("[]")){
                return ResponseEntity.ok("上传成功");
            }
            else throw new FileDuplicateException(fileMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     *  判断上传文件与已有文件是否存在冲突
     * @param files 上传文件
     * @return 冲突内容
     * @throws IOException
     */
    @GetMapping(value = "/conflicts", produces = "application/json; charset=UTF-8")
    @ApiOperation(value = "文件冲突检测", notes = "检查上传文件和已有文件之间的内容冲突信息")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<List<String>> getFileConflicts(@RequestHeader("files") MultipartFile[] files) throws IOException {

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

    @PostMapping("/validate")
    @ApiOperation(value = "批注检测", notes = "上传文件前检查所填批注是否正确")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<List<String>> validateComments(@RequestHeader("files") MultipartFile[] files) {
        List<String> validationResults = validateService.validate(files);
        return ResponseEntity.ok().body(validationResults);
    }

}
