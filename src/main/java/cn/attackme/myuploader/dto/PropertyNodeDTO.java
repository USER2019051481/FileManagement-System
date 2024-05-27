package cn.attackme.myuploader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PropertyNodeDTO {
    private String key ;
    private String value ;
    private String type;
    private List<PropertyNodeDTO> child ;

    public PropertyNodeDTO(String propertyName, String propertyValue,String type) {
        this.key = propertyName;
        this.value = propertyValue;
        this.type =type ;
    }

    public PropertyNodeDTO addChild(PropertyNodeDTO childNode) {
        if(childNode==null){
            log.warn("没有子节点");
            return this;
        }

        if (this.child == null) {
            this.child = new ArrayList<>();
        }

        this.child.add(childNode);
        return this ;

    }

}
