package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/QueryFile")
@CrossOrigin
@Slf4j
@Api(tags = "Query File", description = "文件查询")
public class FileQueryController {
    @Autowired
    private FileService fileService;

    @GetMapping
    @ApiOperation(value = "文件查询", notes = "认证后用户只能获得所属医院的文件信息")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> queryFile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String hospitalName = (String) authentication.getPrincipal();
        List<String> result = fileService.queryFiles(hospitalName);
        if (result.isEmpty()) {
            return ResponseEntity.ok("所属医院文件为空");
        } else {
            return ResponseEntity.ok(result);
        }
    }
}
