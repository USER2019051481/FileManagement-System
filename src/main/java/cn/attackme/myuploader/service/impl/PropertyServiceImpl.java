package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.service.PropertyService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class PropertyServiceImpl implements PropertyService {
    @Override
    @Transactional
    public void scanAndStoreDomainValues(PropertiesRespository propertiesRespository , String basePackage) {
        // 扫描domain包下的所有Java文件
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> domainClasses = reflections.getTypesAnnotatedWith(Entity.class);
        for(Class<?> clazz : domainClasses){
            processClass(clazz,propertiesRespository,new HashMap<>()) ;
        }
    }

    private void processClass(Class<?> clazz, PropertiesRespository propertiesRespository, Map<String,String> propertyMap) {

        String className = clazz.getSimpleName() ;

        ApiModel annotationClassName = clazz.getAnnotation(ApiModel.class);
        String classNameChinese = "" ;
        if(annotationClassName != null){
            classNameChinese = annotationClassName.description();
        }
        if(className.equals("Object")){
            // 如果是Object，则结束;
            log.debug(className +"==Object");
            return ;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(Column.class);
            if (annotation == null) {
                continue;
            }
            String fieldName = field.getName();
            ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
            String property = (apiModelProperty != null) ? apiModelProperty.value() : "没有中文注解！";
            propertyMap.put(fieldName, property);
        }


        // 循环处理父类属性
        // 处理父类属性
        Class<?> superClass = clazz.getSuperclass() ;
        while(superClass!= java.lang.Object.class && superClass != null){
            Field[] superFields = superClass.getDeclaredFields();
            for (Field superField : superFields) {
                Annotation superAnnotation = superField.getAnnotation(Column.class);
                if (superAnnotation == null) {
                    continue;
                }
                String superFieldName = superField.getName();
                ApiModelProperty superApiModelProperty = superField.getAnnotation(ApiModelProperty.class);
                String superProperty = (superApiModelProperty != null) ? superApiModelProperty.value() : "没有中文注解！";
                propertyMap.put(superFieldName, superProperty);
            }
            superClass = superClass.getSuperclass() ;
        }

        if(superClass== java.lang.Object.class){
            saveDomainValues(  propertiesRespository,
                    className,  classNameChinese,
                    propertyMap) ;
        }



    }



    /**
     * 将扫描到的属性值和中文值，存入数据库
     * @param propertiesRespository
     * @param className
     * @param classNameChinese
     * @param propertyMap
     */
    @Override
    public void saveDomainValues(PropertiesRespository propertiesRespository,
                                 String className, String classNameChinese,
                                 Map<String,String > propertyMap) {
        PropertyEntity propertyEntity = propertiesRespository.findByClassName(className);
        if(propertyEntity != null){
            //数据库已经存在相应记录，进行更新操作
            propertyEntity.setPropertyMap(propertyMap);
            propertiesRespository.save(propertyEntity) ;

        }else{
            // 将属性名和注解值存入数据库中
            PropertyEntity newpropertyEntity = new PropertyEntity() ;
            newpropertyEntity.setClassName(className);
            newpropertyEntity.setClassChineseName(classNameChinese);
            newpropertyEntity.setPropertyMap(propertyMap);
            propertiesRespository.save(newpropertyEntity) ;
        }
    }
}
