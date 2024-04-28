package cn.attackme.myuploader.service;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.entity.PropertyMapEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;

import java.util.Map;
import java.util.Set;

public interface PropertyService {
    // 通过反射扫描domain下的值
    void scanAndStoreDomainValues(PropertiesRespository propertiesRespository , String basePackage) throws IllegalAccessException, InstantiationException;

    // 将扫描到的值存入数据库
    void saveDomainValues(PropertiesRespository propertiesRespository,
                          PropertyEntity newpropertyEntity, Set<PropertyMapEntity> propertyMapEntities);

    // 将数据库中的值取出来进行拼接
    String concatenateDatabaseValues(PropertiesRespository propertiesRespository,String className,String rootPackageName) ;
}
