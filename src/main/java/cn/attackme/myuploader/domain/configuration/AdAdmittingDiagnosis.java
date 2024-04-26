package cn.attackme.myuploader.domain.configuration;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入院诊断
 */
@Data
@ApiModel(description = "入院诊断")
//@Entity
@Table(name = "ad_admitting_diagnosis")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AdAdmittingDiagnosis implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    @Column(name = "luna_sort")
    private Integer sort;

    /**
     * 高危标记
     */
    @ApiModelProperty(value = "高危标记")
    @Column(name = "highrisk")
    private Boolean highrisk;

    /**
     * 诊断
     */
    @ApiModelProperty(value = "诊断")
    @Column(name = "diagnosis")
    private String diagnosis;

    /**
     * 诊断编码
     */
    @ApiModelProperty(value = "诊断编码")
    @Column(name = "diagnosis_code")
    private String diagnosisCode;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @Column(name = "note")
    private String note;

    /**
     * 时间
     */
    @ApiModelProperty(value = "时间")
    @Column(name = "create_date")
    private LocalDate createDate;

    /**
     * 医生
     */
    @ApiModelProperty(value = "医生")
    @Column(name = "doctor")
    private String doctor;

    /**
     * 删除
     */
    @ApiModelProperty(value = "删除")
    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "deletedoctor")
    private String deletedoctor;

    /**
     * 删除时间
     */
    @ApiModelProperty(value = "删除时间")
    @Column(name = "delete_date")
    private LocalDate deleteDate;

    /**
     * 其他
     */
    @ApiModelProperty(value = "其他")
    @Column(name = "other")
    private Boolean other;

    @Column(name = "other_note")
    private String otherNote;

    /*@ManyToOne
    @JsonIgnore
    private Admission admission;*/

}
