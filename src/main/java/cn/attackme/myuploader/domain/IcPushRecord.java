package cn.attackme.myuploader.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 三病推送记录
 */
@Entity
@Table(name = "ic_push_record")
@Getter
@Setter
public class IcPushRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 推送时间 */
    @Column(name = "push_time")
    private LocalDateTime pushTime;

    /** 三病推送患者 */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "icPushRecord")
    private Set<IcPushPatient> icPushPatients;

    /** 三病推送 - 科室 */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ic_push_record_department",
            joinColumns = @JoinColumn(name = "ic_push_record_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "ic_push_department_id", referencedColumnName = "id"))
        private Set<IcPushDepartment> icPushDepartments;

    /** 三病推送 - 医生 */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ic_push_record_doctor",
            joinColumns = @JoinColumn(name = "ic_push_record_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "ic_push_doctor_id", referencedColumnName = "id"))
    private Set<IcPushDoctor> IcPushPatient;



}
