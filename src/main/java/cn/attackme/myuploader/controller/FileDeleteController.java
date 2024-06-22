package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.utils.exception.FileNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/File")
@CrossOrigin
@Api(tags = "Delete File", description = "文件删除")
public class FileDeleteController {
    @Autowired
    private FileService fileService;

    @ResponseBody
    @DeleteMapping("/Delete")
    @ApiOperation(value = "删除文件", notes = "通过文件名删除文件，可以实现多文件删除、数据库和本地同步删除")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> deleteFiles(@RequestBody String fileData) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("身份未认证");
        }
        String hospitalName = (String) authentication.getPrincipal();
        try{
            String filemessage = fileService.deleteFiles(fileData, hospitalName);
            if(filemessage.equals("[]")){
                return ResponseEntity.ok("删除成功");
            }
            else throw new FileNotFoundException(filemessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
