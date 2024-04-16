package cn.attackme.myuploader.controller;


import cn.attackme.myuploader.repository.PropertiesRespository;

import cn.attackme.myuploader.service.PropertyService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/Properties")
@Slf4j
public class PropertiesController {

    @Resource
    private PropertiesRespository propertiesRespository ;
    @Autowired
    private PropertyService propertyService ;

    /**
     * domain包的路径
     */
    @Value("${basePackage.path}")
    private  String basePackage ;

    /**
     * 扫描domian中的属性值和中文值并存储到数据库
     * @return
     */
    @GetMapping("/scan")
    public ResponseEntity<String> storePropertiesToDatabase(){
        propertyService.scanAndStoreDomainValues(propertiesRespository,basePackage);
        return ResponseEntity.ok("扫描domain下的属性名和中文值并存入数据库成功！");
    }
}
