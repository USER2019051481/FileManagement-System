package cn.attackme.myuploader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Entity
@Table(name = "property_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 类名
     */
    @Column(name = "class_name")
    private String className;

    /**
     * 类的中文解析
     */
    @Column(name = "class_chinese_name")
    private String ClassChineseName ;

    /**
     * 属性名：注释值
     */
    @ElementCollection
    @MapKeyColumn(name = "property_name")
    @Column(name = "property_value")
    private Map<String, String> propertyMap = new HashMap<>();



}
