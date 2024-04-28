package cn.attackme.myuploader.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "property_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

    @Column(name = "created_time")
    private LocalDateTime date ;


    // 对应 PropertyMapEntity 中的 "propertyentity" 字段
    @OneToMany(mappedBy = "propertyentity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference // 作为主控方
    private Set<PropertyMapEntity> propertyMaps = new HashSet<>();




}
