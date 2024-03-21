package cn.attackme.myuploader.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ExtractingKeysController {
    @PostMapping("/extractKeys")
    public List<String> extractKeys(@RequestBody Map<String, Object> payload) {
        List<String> annotationNames = new ArrayList<>();

        // 使用 Jackson 解析 JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(payload, JsonNode.class);

        // 递归处理 JSON
        extractNamesFromJsonNode(jsonNode, "", annotationNames);

        // 返回提取出的批注名称列表
        return annotationNames;
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
                    // 如果是数组，则递归处理每个元素
                    for (int i = 0; i < fieldValue.size(); i++) {
                        extractNamesFromJsonNode(fieldValue.get(i), prefix + fieldName + "[" + i + "].", names);
                    }
                } else if (fieldValue.isObject()) {
                    // 如果是对象，则递归处理子节点
                    extractNamesFromJsonNode(fieldValue, prefix + fieldName + ".", names);
                }
            }
        }
    }
}
