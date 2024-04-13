package cn.attackme.myuploader.controller;


import cn.attackme.myuploader.entity.FileEntity;
import cn.attackme.myuploader.repository.FileRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 下载批注值
 */
@Controller
@RequestMapping("/DownloadKeys")
@Api(tags = "Download Keys", description = "下载参数")
public class ExtractingKeysDownloadController {

    @Autowired
    private FileRepository fileRepository ;

    @GetMapping("/{filename:.+}")
    @ApiOperation(value = "下载参数", notes = "根据文件名从数据库下载解析的参数")
    @ApiImplicitParam(name = "Authorization", value = "Bearer 访问令牌", required = true, dataTypeClass = String.class, paramType = "header")
    public ResponseEntity<String> downloadExtractingKeys(@PathVariable String filename) {
        FileEntity file = fileRepository.getByName(filename);

        System.out.println("file:"+file.getExtractKeys_data() +file.getId()+file.getUpload_time() );
        return  ResponseEntity.ok().body(file.getExtractKeys_data()) ;
    }
}
