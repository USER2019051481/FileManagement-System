package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.service.PropertyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

@RestController
@RequestMapping("/PropertiesDownload")
public class PropertiesDownloadController {
    @Autowired
    private PropertiesRespository propertiesRespository ;
    @Autowired
    private PropertyService propertyService ;

    @Value("${basePackage.root}")
    private String rootPackageName;

    /**
     * 通过className返回相应PropertyEntity（查找数据库中最新的数据）
     * @param className
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/download")
    public ResponseEntity<String> downloadProperties(@RequestHeader String className) throws JsonProcessingException {
        // 从数据库取出相应className里面的classChineseName和propertyMaps。
        String propertyString = propertyService.concatenateDatabaseValues(propertiesRespository, className,rootPackageName);
        return ResponseEntity.ok().body(propertyString);
    }
}
