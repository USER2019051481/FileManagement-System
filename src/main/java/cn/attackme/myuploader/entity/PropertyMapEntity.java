package cn.attackme.myuploader.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "property_map")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PropertyMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 添加 ID 生成策略
    private Long id;

    @Column(name = "property_name")
    private String propertyName ;

    @Column(name = "property_value")
    private String propertyValue ;

    // 是否是数组
    @Column(name = "property_type")
    private String propertyType ;

    // 是否关联了其他表
    @Column(name = "is_linked")
    private String isLinked ;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_table_id")
    private PropertyEntity propertyentity ;







}
