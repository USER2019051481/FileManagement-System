package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.entity.PropertyMapEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.repository.PropertyMapRespository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PropertyParseServiceImpl {

    @Autowired
    private PropertiesRespository propertiesRespository;
    @Autowired
    private PropertyMapRespository propertyMapRespository;

    @Transactional
    public void processClassNode(LinkedHashMap<String,Object> map){
        PropertyEntity propertyEntity = new PropertyEntity();
        if (map.get("title") != null) {
            propertyEntity.setClassName(map.get("title").toString().replace("\"", ""));
        }
        if (map.get("description") != null) {
            propertyEntity.setClassChineseName(map.get("description").toString().replace("\"", ""));
        }
        propertyEntity.setDate(LocalDateTime.now());
        propertiesRespository.save(propertyEntity);
        if (map.get("properties") instanceof ObjectNode) {
            getProperty((JsonNode) map.get("properties"),propertyEntity);
        }
    }

    private void processPropertyNode(LinkedHashMap<String,Object> map,PropertyEntity propertyEntity) {
        PropertyMapEntity propertyMapEntity = new PropertyMapEntity();
        if (map.get("name") != null) {
            propertyMapEntity.setPropertyName(map.get("name").toString().replace("\"", ""));
        }
        if (map.get("type") != null) {
            propertyMapEntity.setPropertyType(map.get("type").toString().replace("\"", ""));
        }
        if (map.get("description") != null ) {
            propertyMapEntity.setPropertyValue(map.get("description").toString().replace("\"", ""));
        }
        if (map.containsKey("$ref")) {
            String refValue = map.get("$ref").toString().replace("\"", "");
            String linkedValue = refValue.substring(refValue.lastIndexOf('/') + 1);
            propertyMapEntity.setIsLinked(linkedValue);
        } else if(map.containsKey("items")){
            JsonNode items = (JsonNode) map.get("items");
            Iterator<Map.Entry<String, JsonNode>> childs = items.fields();
            while (childs.hasNext()){
                Map.Entry<String, JsonNode> child = childs.next();
                if(child.getKey().equals("$ref")){
                    String refValue = child.getValue().asText();
                    String linkedValue = refValue.substring(refValue.lastIndexOf('/') + 1);
                    propertyMapEntity.setIsLinked(linkedValue);
                }
            }
        }
        else {
            propertyMapEntity.setIsLinked(null);
        }
        propertyMapEntity.setPropertyentity(propertyEntity);
        propertyMapRespository.save(propertyMapEntity);
    }

    public void getClass(JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> roots = jsonNode.fields();
        while(roots.hasNext()){
            Map.Entry<String, JsonNode> root = roots.next();
            Iterator<Map.Entry<String, JsonNode>> childs = root.getValue().fields();
            LinkedHashMap<String,Object> property = new LinkedHashMap<>();
            while (childs.hasNext()){
                Map.Entry<String, JsonNode> child = childs.next();
                property.put(child.getKey(), child.getValue());
            }
            processClassNode(property);
        }
    }

    public void getProperty(JsonNode jsonNode,PropertyEntity propertyEntity) {
        Iterator<Map.Entry<String, JsonNode>> roots = jsonNode.fields();
        while(roots.hasNext()){
            Map.Entry<String, JsonNode> root = roots.next();
            Iterator<Map.Entry<String, JsonNode>> childs = root.getValue().fields();
            LinkedHashMap<String,Object> property = new LinkedHashMap<>();
            property.put("name",root.getKey());
            while (childs.hasNext()){
                Map.Entry<String, JsonNode> child = childs.next();
                property.put(child.getKey(), child.getValue());
            }
            processPropertyNode(property,propertyEntity);
        }
    }


}
