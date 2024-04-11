package cn.attackme.myuploader.service;

import cn.attackme.myuploader.repository.PropertiesRespository;

import java.util.Map;

public interface PropertyService {
    // 通过反射扫描domain下的值
    void scanAndStoreDomainValues(PropertiesRespository propertiesRespository , String basePackage);

    // 将扫描到的值存入数据库
    void saveDomainValues(PropertiesRespository propertiesRespository,
                          String className, String classNameChinese, Map<String,String > propertyMap);
}
