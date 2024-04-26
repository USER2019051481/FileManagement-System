package cn.attackme.myuploader.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 三病推送患者
 */
@Entity
@Table(name = "ic_push_patient")
@Getter
@Setter
public class IcPushPatient implements Serializable {

    @PrePersist
    public void prePersist(){
        if (getDepartmentInclusion()==null) setDepartmentInclusion(Boolean.FALSE);
        if (getDiagnosisInclusion()==null) setDiagnosisInclusion(Boolean.FALSE);
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 患者名称 */
    @Column(name = "name")
    private String name;

    /** 患者号 */
    @Column(name = "patient_no")
    private String patientNO;

    /** 患者证件号 */
    @Column(name = "id_no")
    private String idNO;

    /** 挂号记录流水号 */
    @Column(name = "registration_flow_id")
    private String registrationFlowId;

    /** 挂号记录科室名称 */
    @Column(name = "registratio_dep_name")
    private String registrationDepName;

    /** 挂号记录科室号 */
    @Column(name = "registration_dep_id")
    private String registrationDepId;

    /** 挂号记录日期 */
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    /** 是否匹配科室 */
    @Column(name = "department_inclusion")
    private Boolean departmentInclusion;

    /** 是否匹配诊断 */
    @Column(name = "diagnosis_inclusion")
    private Boolean diagnosisInclusion;

    /** 是否OBIS建册 */
    @Column(name = "pregnancy_inclusion")
    private Boolean pregnancyInclusion;

    /** 孕册id */
    @Column(name = "pregnancy_id")
    private Long pregnancyId;

    /** 孕册移动端id */
    @Column(name = "pregnancy_mpuid")
    private String pregnancyMpuid;

    /** 是否梅毒异常 */
    @Column(name = "abnormality_syphilis")
    private Boolean abnormalitySyphilis;

    /** 是否乙肝异常 */
    @Column(name = "abnormality_hbv")
    private Boolean abnormalityHbv;

    /** 是否乙肝DNA异常 */
    @Column(name = "abnormality_hbv_dna")
    private Boolean abnormalityHbvDna;

    /** 是否HIV异常 */
    @Column(name = "abnormality_hiv")
    private Boolean abnormalityHiv;

    /** 三病推送记录 */
    @ManyToOne(fetch = FetchType.LAZY)
    private IcPushRecord icPushRecord;

    /** 三病患者推送记录 */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "icPushPatient")
    private IcPushPatientRecord icPushPatientRecord;

    /** 三病医生推送记录 */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "icPushPatient")
    private Set<IcPushDoctorRecord> icPushDoctorRecords;

}
