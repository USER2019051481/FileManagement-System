package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.dto.PropertyNodeDTO;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.service.PropertyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/PropertiesDownload")
@Api(tags = "Download Properties", description = "参数下载")
public class PropertiesDownloadController {
    @Autowired
    private PropertiesRespository propertiesRespository ;
    @Autowired
    private PropertyService propertyService ;

    @Value("${basePackage.root}")
    private String rootPackageName;

    /**
     * 通过className返回相应PropertyEntity（查找数据库中最新的数据）
     *
     * @param className
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/download")
    @ApiOperation(value = "获取中英文参数", notes = "根据类名返回相应属性的中英文对应参数")
    public ResponseEntity<PropertyNodeDTO> downloadProperties(@RequestHeader String className) throws JsonProcessingException {
        // 从数据库取出相应className里面的classChineseName和propertyMaps。
        PropertyNodeDTO rootNode = propertyService.concatenateDatabaseValues(propertiesRespository, className , new HashSet<>(),0);


        ObjectMapper mapper = new ObjectMapper();
        try {
            return ResponseEntity.ok(rootNode) ;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }






}
