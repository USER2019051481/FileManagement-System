package cn.attackme.myuploader.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 三病医生推送记录
 */
@Entity
@Table(name = "ic_push_doctor_record")
@Getter
@Setter
public class IcPushDoctorRecord implements Serializable {

    @PrePersist
    public void prePersist(){
        if (getSuccessed()==null) setSuccessed(Boolean.FALSE);
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 推送时间 */
    @Column(name = "push_time")
    private LocalDateTime pushTime;

    /** 推送目标 */
    @Column(name = "target")
    private String target;

    /** 推送标识号 */
    @Column(name = "target_id")
    private String targetId;

    /** 推送内容 */
    @Column(name = "message")
    private String message;

    /** 是否成功 */
    @Column(name = "successed")
    private Boolean successed;

    /** 备注 */
    @Column(name = "remark")
    private String remark;

    /** 三病推送患者 */
    @ManyToOne(fetch = FetchType.LAZY)
    private IcPushPatient icPushPatient;

}
