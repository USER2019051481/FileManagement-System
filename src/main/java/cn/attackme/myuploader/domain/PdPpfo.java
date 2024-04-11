package cn.attackme.myuploader.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 产后随访
 */
@ApiModel(description = "产后随访")
@Entity
public class PdPpfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 孕妇并发症（复选框）
     */
    @ApiModelProperty(value = "孕妇并发症（复选框）")
    @Column(name = "complication")
    private Boolean complication;

    @Column(name = "complication_note")
    private String complicationNote;

    /**
     * 孕妇情况
     */
    @ApiModelProperty(value = "孕妇情况")
    @Column(name = "personal_profile")
    private String personalProfile;

    /**
     * 新生儿情况
     */
    @ApiModelProperty(value = "新生儿情况")
    @Column(name = "fetus_profile")
    private String fetusProfile;

    /**
     * 随访情况
     */
    @ApiModelProperty(value = "随访情况")
    @Column(name = "follow_up_profile")
    private String followUpProfile;

    /**
     * 随访日期
     */
    @ApiModelProperty(value = "随访日期")
    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    // ==================
    // '相似项目';
    @Column(name = "conform_item")
    private String conformItem;
    // '不符项目';
    @Column(name = "discrepancy_item")
    private String discrepancyItem;
    // '询问对象';
    @Column(name = "ask_object")
    private String askObject;

    // '新生儿筛查结果';
    @Column(name = "screen_result")
    private String screenResult;
    // '新生儿生后复查结果';
    @Column(name = "review_result")
    private String reviewResult;
    // '新生儿-其它';
    @Column(name = "fetus_other")
    private String fetusOther;
    // ==================

    // 是否多胎
    @Column(name = "multiple")
    private Boolean multiple;

    // 是否住院
    @Column(name = "inpatient")
    private Boolean inpatient;

    /**
     * 随访类型
     */
    @Column(name = "visit_type")
    private Integer visitType;

    /**
     * 随访人
     */
    @ApiModelProperty(value = "随访人")
    @Column(name = "follow_up_person")
    private String followUpPerson;

    @ApiModelProperty(value = "预产期后第几天")
    @Column(name = "follow_up_days")
    private Integer followUpDays;

    /**
     *   假删除标识
     */
    @ApiModelProperty(value = "假删除标识")
    @Column(name = "delete_flag")
    private  Boolean deleteFlag;

    // 3.18 ---------------------------------------------
    /**
     * 分娩方式
     */
    @ApiModelProperty(value = "分娩方式")
    @Column(name = "delivery_mode")
    private Integer deliveryMode;

    /**
     * 分娩方式备注
     */
    @ApiModelProperty(value = "分娩方式备注")
    @Column(name = "delivery_mode_note")
    private String deliveryModeNote;

    /**
     * 分娩孕周
     */
    @ApiModelProperty(value = "分娩孕周")
    @Column(name = "delivery_gestational_week")
    private String deliveryGestationalWeek;




    // =====================================
    // 设置产后随访的是否多胎胎

}
