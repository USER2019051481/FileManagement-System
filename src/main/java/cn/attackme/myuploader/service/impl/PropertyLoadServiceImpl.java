package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.dto.PropertyNodeDTO;
import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.entity.PropertyMapEntity;
import cn.attackme.myuploader.repository.PropertiesRespository;
import cn.attackme.myuploader.repository.PropertyMapRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PropertyLoadServiceImpl {
    @Autowired
    private PropertiesRespository propertiesRespository;

    public List<Map<String, String>> getAllProperties() {
        List<PropertyEntity> properties = propertiesRespository.findAll();
        return properties.stream()
                .map(property -> {
                    Map<String, String> propertyMap = new HashMap<>();
                    propertyMap.put("key", property.getClassName());  // 假设 PropertyEntity 有 getKey() 方法
                    propertyMap.put("value", property.getClassChineseName()); // 假设 PropertyEntity 有 getValue() 方法
                    return propertyMap;
                })
                .collect(Collectors.toList());
    }

    public PropertyNodeDTO getPropertyByClassName(String className) {
        Optional<PropertyEntity> optionalPropertyEntity = Optional.ofNullable(propertiesRespository.findByClassName(className));
        PropertyEntity propertyEntity = optionalPropertyEntity.orElseThrow(() ->
                new NoSuchElementException("PropertyEntity not found for class name: " + className));

        List<Long> visitedIds = new ArrayList<>();
        // 调用 buildPropertyNode 方法构建 PropertyEntity 节点
        PropertyNodeDTO entityNodeDTO = buildPropertyNode(propertyEntity, visitedIds);

        // 创建最外层的 property 节点
        PropertyNodeDTO outerPropertyNodeDTO = new PropertyNodeDTO();
        outerPropertyNodeDTO.setKey(className.substring(0, 1).toLowerCase() + className.substring(1));// 使用类名作为 key
        outerPropertyNodeDTO.setValue(propertyEntity.getClassChineseName()); // value 为 null
        outerPropertyNodeDTO.setType("property"); // 类型为 property
        outerPropertyNodeDTO.setChild(Collections.singletonList(entityNodeDTO)); // 设置子节点
        return outerPropertyNodeDTO;
    }

    private PropertyNodeDTO buildPropertyNode(PropertyEntity entity, List<Long> visitedIds) {
        if (visitedIds.contains(entity.getId())) {
            PropertyNodeDTO nodeDTO = new PropertyNodeDTO();
            nodeDTO.setKey(entity.getClassName());
            nodeDTO.setValue(entity.getClassChineseName());
            nodeDTO.setType("class");
            nodeDTO.setChild(null);
            return nodeDTO;
        }
        visitedIds.add(entity.getId());

        PropertyNodeDTO nodeDTO = new PropertyNodeDTO();
        nodeDTO.setKey(entity.getClassName());
        nodeDTO.setValue(entity.getClassChineseName());
        nodeDTO.setType("class");

        List<PropertyMapEntity> propertyMaps = new ArrayList<>(entity.getPropertyMaps());
        List<PropertyNodeDTO> childDTOs = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>(); // 用于记录已经处理过的key

        for (PropertyMapEntity map : propertyMaps) {
            // 处理链接的属性
            if (map.getIsLinked() != null) {
                Optional<PropertyEntity> optionalChildEntity =
                        Optional.ofNullable(propertiesRespository.findByClassName(map.getIsLinked()));
                PropertyEntity childEntity = optionalChildEntity.orElseThrow(() ->
                        new NoSuchElementException("PropertyEntity not found for class name: " + map.getPropertyName())
                );

                PropertyNodeDTO childNodeDTO = buildPropertyNode(childEntity, visitedIds);

                // 如果子节点是 class 且其子节点为 null，则不添加该子节点
                if (childNodeDTO.getType().equals("class") && childNodeDTO.getChild() == null) {
                    // 跳过添加该子节点
                    continue;
                }

                // 将子节点的类型设置为 property
                PropertyNodeDTO propertyNodeDTO = new PropertyNodeDTO();
                propertyNodeDTO.setKey(map.getPropertyName());
                propertyNodeDTO.setValue(map.getPropertyValue());
                propertyNodeDTO.setType("property");

                if (!seenKeys.contains(map.getPropertyName())) { // 检查key是否已处理
                    propertyNodeDTO.setChild(Collections.singletonList(childNodeDTO)); // 将之前的子节点作为其子节点
                    seenKeys.add(map.getPropertyName()); // 标记该key已处理
                    childDTOs.add(propertyNodeDTO);
                }
            }

            // 处理非链接的属性
            if (!"id".equals(map.getPropertyName())) { // 检查属性名称是否为 "id"
                PropertyNodeDTO propertyNodeDTO = new PropertyNodeDTO();
                propertyNodeDTO.setKey(map.getPropertyName());
                propertyNodeDTO.setValue(map.getPropertyValue());
                propertyNodeDTO.setType("property");

                if (!seenKeys.contains(map.getPropertyName())) { // 检查key是否已处理
                    propertyNodeDTO.setChild(null); // 设置 child 为 null
                    seenKeys.add(map.getPropertyName()); // 标记该key已处理
                    childDTOs.add(propertyNodeDTO);
                }
            }
        }

        childDTOs.sort(Comparator.comparing(PropertyNodeDTO::getKey));

        // 如果有子节点，则将其添加到当前节点
        if (!childDTOs.isEmpty()) {
            nodeDTO.setChild(childDTOs);
        } else {
            nodeDTO.setChild(null); // 没有子节点时设置为 null
        }

        return nodeDTO;
    }

}
