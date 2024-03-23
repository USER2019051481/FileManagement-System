package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.AnnotationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/ExtractKeys")
public class ExtractingKeysController {
    @Autowired
    private AnnotationService annotationService ;

    /**
     * 提取JSON中键，存入数据库file表格的extractKeys_data属性中
     * @param payload OBIS的JSON数值
     * @param filename  对应的模板名称
     */

    @PostMapping("/{filename:.+}")
    public ResponseEntity extractKeys(@RequestBody Map<String, Object> payload, @PathVariable String filename ) {
        List<String> annotationNames = new ArrayList<>();

        // 使用 Jackson 解析 JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(payload, JsonNode.class);

        // 递归处理 JSON
        extractNamesFromJsonNode(jsonNode, "", annotationNames);


        // 提取出的批注名称列表转为String类型
        String annotationNamesString = annotationNames.toString();
        // 根据前端给的模板名称，存入数据库
        annotationService.saveAnnotationNames(annotationNamesString,filename);

        // 返回解析成功
        return ResponseEntity.ok(200);
    }

    private void extractNamesFromJsonNode(JsonNode jsonNode, String prefix, List<String> names) {
        if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                if (fieldValue.isValueNode()) {
                    // 如果是叶子节点，则添加到结果列表中
                    names.add(prefix + fieldName );
                } else if (fieldValue.isArray()) {
                    if (fieldValue.size()==0) {
                        // 如果是空数组，则添加数组名作为键
                        names.add(prefix + fieldName + "[0]");
                    } else {
                        // 如果是非空数组，则递归处理每个元素
                        for (int i = 0; i < fieldValue.size(); i++) {
                            extractNamesFromJsonNode(fieldValue.get(i), prefix + fieldName + "[" + i + "].", names);
                        }
                    }
                } else if (fieldValue.isObject()) {
                    // 如果是对象，则递归处理子节点
                    extractNamesFromJsonNode(fieldValue, prefix + fieldName + ".", names);
                }
            }
        }
    }
}
