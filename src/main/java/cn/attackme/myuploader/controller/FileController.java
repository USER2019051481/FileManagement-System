package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.utils.HospitalUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/file")
@CrossOrigin
@Slf4j
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;
    @Autowired
    private FileService fileService;
    @Autowired
    private HospitalUtil hospitalUtil;

    @ResponseBody
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传", notes = "实现多文件上传，控制总文件大小<100000MB，同时判断文件重命名和内容重复情况")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            String hospital = hospitalUtil.getAuthenticatedHospital();
            String fileMessage = fileService.uploadFiles(files, hospital);
            if (fileMessage.equals("[]")){
                return ResponseEntity.ok("上传成功");
            }
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileMessage);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping("/query")
    @ApiOperation(value = "文件查询", notes = "认证后用户只能获得所属医院的文件信息")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> queryFiles() {
        try{
            String hospital = hospitalUtil.getAuthenticatedHospital();
            String result = fileService.queryFiles(hospital);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("/modify")
    @ApiOperation(value = "文件修改", notes = "通过文件名更新文件，重命名或者文件内容")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> modifyFiles(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "newName", required = false) String newName) {
        try {
            String hospital = hospitalUtil.getAuthenticatedHospital();
            String result = fileService.modifyFiles(file,newName, hospital);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @ResponseBody
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除文件", notes = "通过文件名删除文件，可以实现多文件删除")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<?> deleteFiles(@RequestBody String fileData) {
        try{
            String hospital = hospitalUtil.getAuthenticatedHospital();
            String fileMessage = fileService.deleteFiles(fileData, hospital);
            if(fileMessage.equals("[]")){
                return ResponseEntity.ok("删除成功");
            }
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileMessage);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 根据文件名，返回相应文件
     * @param filename 文件名
     * @return 返回文件
     */
    @GetMapping("/download/{filename:.+}")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    @ApiOperation(value = "下载文件", notes = "根据文件名下载对应的文件")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        try {
            String hospital = hospitalUtil.getAuthenticatedHospital();
            // 构造文件路径
            String filePathString = uploadPath + "/" + hospital + "/" + filename ;
            Path filePath = Paths.get(filePathString);
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
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
