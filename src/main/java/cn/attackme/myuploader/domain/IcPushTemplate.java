package cn.attackme.myuploader.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.mapping.Set;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "三病推送模板")
@Entity
@Table(name = "ic_push_template")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class IcPushTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "名称")
    @Column(name = "name")
    private String name;


    @ApiModelProperty(value = "编号")
    @Column(name = "code")
    private String code;

    @ApiModelProperty(value = "内容")
    @Column(name = "content")
    private String content;

//    /**
//     * 属性名：注释值
//     */
//    @ElementCollection
//    @MapKeyColumn(name = "property_name")
////    @Column(name = "property_value")
//    private Map<String, Address> propertyMap = new HashMap<>();


//    @ApiModelProperty(value = "排序")
//    @Column(name = "sort")
//    private Long sort;


}
