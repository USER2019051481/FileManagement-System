package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/File")
@CrossOrigin
@Slf4j
@Api(tags = "Query File", description = "文件查询")
public class FileQueryController {
    @Autowired
    private FileService fileService;

    @ResponseBody
    @GetMapping("/Query")
    @ApiOperation(value = "文件查询", notes = "认证后用户只能获得所属医院的文件信息")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> queryFile() throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("身份未认证");
        }
        String hospitalName = (String) authentication.getPrincipal();
        String result = fileService.queryFiles(hospitalName);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("所属医院文件为空");
        } else {
            return ResponseEntity.ok(result);
        }
    }
}
