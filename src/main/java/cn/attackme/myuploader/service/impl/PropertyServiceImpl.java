package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.entity.PropertyMapEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.service.PropertyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Log;
import org.apache.poi.ss.formula.functions.T;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PropertyServiceImpl implements PropertyService {
    /**
     * 扫描domain包下的所有java文件
     * @param propertiesRespository
     * @param basePackage
     */
    @Override
    @Transactional
    public void scanAndStoreDomainValues(PropertiesRespository propertiesRespository , String basePackage) throws IllegalAccessException, InstantiationException {
        // 扫描domain包下的所有Java文件
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> domainClasses = reflections.getTypesAnnotatedWith(Entity.class);
        for(Class<?> clazz : domainClasses){
            processClass(clazz,propertiesRespository,new HashSet<>()) ;
        }
    }

    /**
     * 处理每个类
     * @param clazz
     * @param propertiesRespository
     * @param propertyMapEntities
     */
    private void processClass(Class<?> clazz, PropertiesRespository propertiesRespository, Set<PropertyMapEntity> propertyMapEntities) throws IllegalAccessException, InstantiationException {

        // 类名称
        String className = clazz.getSimpleName() ;

        // 类的中文描述
        ApiModel annotationClassName = clazz.getAnnotation(ApiModel.class);
        String classNameChinese = "" ;
        if(annotationClassName != null){
            classNameChinese = annotationClassName.description();
        }else{
            classNameChinese = "该类没有中文描述" ;
        }

        // 先往PropertyEntity添加类名和类描述
        PropertyEntity newpropertyEntity = new PropertyEntity() ;
        newpropertyEntity.setClassName(className);
        newpropertyEntity.setClassChineseName(classNameChinese);

        // 判断是否为Object
        if(className.equals("Object")){
            // 如果是Object，则结束;
            log.debug(className +"==Object");
            return ;
        }


        // 处理该类中的属性值和中文值
        Field[] fields = clazz.getDeclaredFields();
        processFields(fields,newpropertyEntity,propertyMapEntities) ;


        // 循环处理父类属性
        // 处理父类属性
        Class<?> superClass = clazz.getSuperclass() ;
        while(superClass!= Object.class && superClass != null){
            Field[] superFields = superClass.getDeclaredFields();
            processFields(superFields,newpropertyEntity,propertyMapEntities);
            superClass = superClass.getSuperclass() ;
        }

        // 将结果存放入数据库
        if(superClass== Object.class){
            saveDomainValues(propertiesRespository,
                    newpropertyEntity,
                    propertyMapEntities) ;
        }



    }


    private void processFields(Field[] fields, PropertyEntity newPropertyEntity, Set<PropertyMapEntity> propertyMapEntities) throws IllegalAccessException {
        // 要检查的注解类型（使用注解的 Class 对象）
        Class<? extends Annotation>[] relatedAnnotations = new Class[]{
                OneToOne.class,
                ManyToOne.class,
                ManyToMany.class,
                OneToMany.class
        };

        for (Field field : fields) {
            // 检查是否有关系注解
            boolean hasRelationshipAnnotation = Arrays.stream(relatedAnnotations)
                    .anyMatch(annotationClass -> field.getAnnotation(annotationClass) != null); // 传递注解的 Class 对象

            if (hasRelationshipAnnotation) {
                // 如果有关系注解，处理关联
                processRelatedClass(field, newPropertyEntity, propertyMapEntities);
                continue;
            }

            if(field.getAnnotation(ElementCollection.class) != null){
                // TODO:处理Map
                processElementCollection(field,newPropertyEntity,propertyMapEntities) ;
                continue;
            }

            // 检查是否有 Column 注解
            if (field.getAnnotation(Column.class) != null) {
                createAndAddToPropertyMaps(field, newPropertyEntity, propertyMapEntities);
            }


        }
    }

    /**
     * 处理ElementCollection注解
     * @param field
     * @param propertyMapEntities
     */
    private void processElementCollection(Field field, PropertyEntity propertyEntity, Set<PropertyMapEntity> propertyMapEntities) {
        PropertyMapEntity propertyMapEntity = createAndAddToPropertyMaps(field,propertyEntity,propertyMapEntities);

        if(Map.class.isAssignableFrom(field.getType())){
            extracted(Map.class,field,propertyMapEntity);
        }else if(Set.class.isAssignableFrom(field.getType())){
            extracted(Set.class,field,propertyMapEntity);
        }else if(List.class.isAssignableFrom(field.getType())){
            extracted(List.class,field,propertyMapEntity);
        }

    }



    /**
     * 创建注解类放入集合中
     * @param newpropertyEntity
     * @param propertyMapEntities
     */
    private PropertyMapEntity createAndAddToPropertyMaps(Field field ,  PropertyEntity newpropertyEntity ,Set<PropertyMapEntity> propertyMapEntities) {
        PropertyMapEntity propertyMapEntity = new PropertyMapEntity() ;
        // 属性的中文注解
        ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
        String property = (apiModelProperty != null) ? apiModelProperty.value() : "没有中文注解！";
        // 属性名
        String fieldName = field.getName() ;
        // 属性类型
        Class<?> type = field.getType();

        propertyMapEntity.setPropertyName(fieldName);
        propertyMapEntity.setPropertyValue(property);
        propertyMapEntity.setPropertyType(type.toString()) ;
        propertyMapEntity.setIsLinked("");
        propertyMapEntity.setPropertyentity(newpropertyEntity); // 属性和类的关联
        propertyMapEntities.add(propertyMapEntity) ; // 该属性加入集合

        return propertyMapEntity ;  //返回PropertyMapEntity
    }

    /**
     * 将关联的表写入数据库
     * @param field
     * @param propertyMapEntities
     */
    private void processRelatedClass(Field field, PropertyEntity propertyEntity, Set<PropertyMapEntity> propertyMapEntities) throws IllegalAccessException {
        PropertyMapEntity propertyMapEntity = createAndAddToPropertyMaps(field,propertyEntity,propertyMapEntities);


        // 确保field属性能够访问
        field.setAccessible(true);
        if(Set.class.isAssignableFrom(field.getType())){
            extracted(Set.class,field, propertyMapEntity);
        }else if(List.class.isAssignableFrom(field.getType())){
            extracted(List.class,field, propertyMapEntity);
        }


    }



    /**
     * 获取泛型里面的信息
     * @param field
     * @param propertyMapEntity
     */
    private static <T>void extracted(Class<T> classType , Field field, PropertyMapEntity propertyMapEntity) {
        // 判断字段是否为Set
        if(classType.isAssignableFrom(field.getType())){
            // 该字段是classType
            // 获取泛型信息
            Type genericType = field.getGenericType();
            if(genericType instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if(actualTypeArguments.length <= 0 ) {
                    log.debug("没有泛型");
                }
                    // 获取泛型信息
                    // 存储入propertyEntity中
                    if(classType.isAssignableFrom(Map.class)){
                        // 判断有没有嵌入类
                        boolean isKeyEntity = ((Class<?>) actualTypeArguments[0]).isAnnotationPresent(Embeddable.class);
                        boolean isValueEntity = ((Class<?>) actualTypeArguments[1]).isAnnotationPresent(Embeddable.class);
                        if(isKeyEntity && isValueEntity){
                            // Map的键和值都是嵌入类
                            propertyMapEntity.setIsLinked("MapKey_entity: "+actualTypeArguments[0].getTypeName()
                                    +" MapValue_entity: "+actualTypeArguments[1].getTypeName()) ;
                            log.debug("!!!!!!!键和值都是嵌入类!!!!!") ;
                        }else if(!isKeyEntity && isValueEntity){
                            // Map的键不是嵌入类，值是嵌入类
                            // 获取@MapKeyColumn中的值
                            String mapKeyName = field.getAnnotation(MapKeyColumn.class).name();
                            propertyMapEntity.setIsLinked("MapKey: "+mapKeyName
                                    +" MapValue_entity: "+actualTypeArguments[1].getTypeName()) ;
                            log.debug("!!!!!!!值是嵌入类!!!!!") ;

                        }else if(isKeyEntity && !isValueEntity){
                            // Map的键是嵌入类，值不是嵌入类

                            String mapValueName = field.getAnnotation(Column.class).name();
                            propertyMapEntity.setIsLinked("MapKey_entity: "+actualTypeArguments[0].getTypeName()
                                    +" MapValue: "+mapValueName) ;
                            log.debug("!!!!!!!键是嵌入类!!!!!") ;
                        }else if(!isKeyEntity && !isValueEntity){
                            // Map的键和值都不是嵌入类
                            String mapKeyName = field.getAnnotation(MapKeyColumn.class).name();
                            String mapValueName = field.getAnnotation(Column.class).name();
                            propertyMapEntity.setIsLinked("MapKey: "+mapKeyName
                                    +" MapValue: "+mapValueName) ;
                        }

                    }else {
                        // 判断有没有
                        if (field.getAnnotation(Column.class) ==null) {
                            propertyMapEntity.setIsLinked(actualTypeArguments[0].getTypeName());
                        }else{
                            propertyMapEntity.setIsLinked(field.getAnnotation(Column.class).name());
                        }

                    }
            }
        }else{
            // 该字段不是classType
            propertyMapEntity.setIsLinked(field.getType().toString());
        }
    }


    /**
     * 将扫描到的属性值和中文值，存入数据库
     * @param propertiesRespository
     * @param propertyMap
     */
    @Override
    public void saveDomainValues(PropertiesRespository propertiesRespository,
                                 PropertyEntity newpropertyEntity,
                                 Set<PropertyMapEntity> propertyMap) {

        // 将数据存入数据库中
        newpropertyEntity.setPropertyMaps(propertyMap);
        newpropertyEntity.setDate(LocalDateTime.now());
        propertiesRespository.save(newpropertyEntity) ;

    }



    private static final Set<String> processedClasses = new HashSet<>(); // 用于记录已处理的类
    // 递归深度计数器
    private static int recursionDepth = 0;

    /**
     * 递归处理数据库中的属性和中文值，并拼接返回给前端
     * @param propertiesRespository
     * @param className
     * @param rootPackageName
     * @return
     */
    @Override
    // 递归拼接属性值
    public String concatenateDatabaseValues(PropertiesRespository propertiesRespository,
                                          String className,
                                          String rootPackageName
                                          ) {
        recursionDepth++;  // 递归深度加1
        try {

            // 检查是否已处理过该类
            if (processedClasses.contains(className)) {
                return "Class already processed in " + className;
            }

            // 将当前类添加到已处理的集合中
            processedClasses.add(className);

            PropertyEntity propertyEntity = propertiesRespository.findFirstByClassNameOrderByDateDesc(className);

            if (propertyEntity == null) {
                return "Property entity not found for class: " + className;
            }

            // 获取到多个属性和中文值
            Set<PropertyMapEntity> propertyMaps = propertyEntity.getPropertyMaps();
            Set<String> propertyDetails = new HashSet<>();

            for (PropertyMapEntity propertyMap : propertyMaps) {
                String propertyName = propertyMap.getPropertyName();
                String propertyValue = propertyMap.getPropertyValue();
                String propertyType = propertyMap.getPropertyType();
                String isLinked = propertyMap.getIsLinked();

                // 判断是否关联到本项目的其他实体类
                boolean classInProject = isClassInProject(propertyType, rootPackageName);

                if (classInProject) {
                    //                log.debug("关联到本项目中的其他实体类===================>"+propertyType);
                    String nestedClassName = ClassNameExtractor(propertyType);
                    // 递归处理关联的类
                    String nestedProperty = concatenateDatabaseValues(propertiesRespository, nestedClassName, rootPackageName);
                    propertyDetails.add(propertyName + ": " + nestedProperty);
                } else {
                    // 判断是否为Set和List
                    boolean collectionType = isListAndSetType(propertyType);
                    if (collectionType) {
                        // 判断Set和List中是否关联了其他类
                        log.debug("==================>该类为Set或者List");
                        boolean linkInProject = isClassInProject(isLinked, rootPackageName);
                        if (linkInProject) {
                            // 关联了其他类
                            log.debug("==================>该Set或者List关联了其他类: " + isLinked);
                            String nestedClassName = ClassNameExtractor(isLinked);
                            // 递归处理关联的类
                            String nestedProperty = concatenateDatabaseValues(propertiesRespository, nestedClassName, rootPackageName);
                            propertyDetails.add(propertyName + "[i]: " + nestedProperty);

                        } else {
                            // 没有关联其他类
                            propertyDetails.add(propertyName + "[i]: " + propertyValue);
                        }
                    } else {
                        // 判断是否为Map
                        boolean ismapType = isMapType(propertyType);
                        if (ismapType) {
                            //是Map
                            String[] strings = extractValueAfterColon(isLinked);
                            propertyDetails.add(propertyName + "." + strings[0] + ": ");
                            propertyDetails.add(propertyName + "." + strings[1] + ": ");
                            // TODO 这里默认map中的值没有关联本项目的其他表
                        } else {
                            propertyDetails.add(propertyName + ": " + propertyValue);
                        }

                    }

                }


            }
            return propertyDetails.toString();

        }finally {
            recursionDepth--;  // 递归深度减1
            if (recursionDepth == 0) {
                processedClasses.clear();  // 只有在递归完全结束时才清空集合
            }

        }


    }


    private String[] extractValueAfterColon(String isLinked) {
        // 正则表达式，匹配两个冒号后面的内容
        String regex = ":\\s*(\\w+).*?:\\s*(\\w+)"; // 匹配两个冒号后的单词
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(isLinked);

        // 存储匹配的内容
        String[] extractedValues = new String[2];

        if (matcher.find()) { // 检查是否找到匹配
            extractedValues[0] = matcher.group(1); // 获取第一个捕获的单词
            extractedValues[1] = matcher.group(2); // 获取第二个捕获的单词
        }

        return extractedValues; // 返回匹配的内容
    }

    private boolean isMapType(String className) {
        // 正则表达式，匹配 Set、List 集合类型，前面有点号，后面没有字母
        String regex = ".*\\.\\b(Map)\\b.*"; // 前面有点号，后面是单词边界
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(className).matches();
    }

    private boolean isListAndSetType(String className) {
        // 正则表达式，匹配 Set、List 集合类型，前面有点号，后面没有字母
        String regex = ".*\\.\\b(Set|List)\\b.*"; // 前面有点号，后面是单词边界
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(className).matches();
    }


    private String ClassNameExtractor(String propertyType) {
        // 首先按空格分割，取出最后一部分
        String[] parts = propertyType.split(" ");
        String fullClassPath = parts[parts.length - 1]; // 获取完整类路径

        // 然后按点号分割，取出类名
        String[] classPathParts = fullClassPath.split("\\."); // `\\.` 表示点号
        String className = classPathParts[classPathParts.length - 1]; // 最后一部分即类名

        return className; // 返回提取的类名
    }

    private boolean isClassInProject(String propertyType, String rootPackageName) {
        // 创建一个正则表达式，匹配 "class " 开头，接着是根包名
        String regex = "^(class\\s+)?" + Pattern.quote(rootPackageName) + "\\..*"; // `^` 表示开头，`\\.` 表示点，`.*` 表示任意字符
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(propertyType).matches();

    }
}
