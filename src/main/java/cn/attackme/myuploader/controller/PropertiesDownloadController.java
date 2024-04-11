package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

@RestController
@RequestMapping("/PropertiesDownload")
public class PropertiesDownloadController {
    @Autowired
    private PropertiesRespository propertiesRespository ;

    /**
     * 通过className返回相应的中文值和属性名
     * @param className
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/download")
    public ResponseEntity<String> downloadProperties(@RequestHeader String className) throws JsonProcessingException {
        PropertyEntity property = propertiesRespository.findAllByClassName(className);
        ObjectMapper objectMapper = new ObjectMapper();
        String propertyString = objectMapper.writeValueAsString(property);
        return ResponseEntity.ok().body(propertyString);
    }
}
