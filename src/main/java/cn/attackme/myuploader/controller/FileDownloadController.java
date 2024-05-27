package cn.attackme.myuploader.controller;

import io.swagger.annotations.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * 文件下载
 */
@Controller
    @RequestMapping("/DownloadFile")
@Api(tags = "Download File ", description = "文件下载")
public class FileDownloadController {
    private static final String DOWNLOAD_DIRECTORY = "./upload/"; // 您的文件存放目录

    /**
     * 根据文件名，返回相应文件
     * @param filename 文件名
     * @return 返回文件
     */
    @GetMapping("/{filename:.+}")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    @ApiOperation(value = "下载文件", notes = "根据文件名下载对应的文件")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        // 构造文件路径
        Path filePath = Paths.get(DOWNLOAD_DIRECTORY).resolve(filename).normalize();
        try {
            // 将文件路径转换为Spring能够识别和处理的资源对象
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf(TEXT_PLAIN_VALUE))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

}
