package cn.attackme.myuploader.domain.configuration;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

/**
 * 报卡抽象类，包含艾梅乙报卡的共有字段
 */
@Data

//@Entity
public abstract class AbstractReportCardEntity extends AbstractAuditingEntity {

    @ApiModelProperty("报告单位")
    @Column(name = "report_organization")
    private String reportOrganization;

    @ApiModelProperty("报告医生")
    @Column(name = "report_doctor")
    private String reportDoctor;

    @ApiModelProperty("联系电话")
    @Column(name = "report_telephone")
    private String reportTelephone;

    @ApiModelProperty("填报日期")
    @Column(name = "report_date")
    private LocalDate reportDate;

    @ApiModelProperty("报告备注")
    @Column(name = "report_note")
    private String reportNote;
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
