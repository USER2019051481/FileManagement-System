package cn.attackme.myuploader.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 三病推送 - 医生
 */
@Entity
@Table(name = "ic_push_doctor")
@Where(clause = "(deleted = false or deleted is null)")
@SQLDelete(sql = "UPDATE ic_push_doctor SET deleted=true WHERE id=?")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class IcPushDoctor implements Serializable {

    @PrePersist
    public void prePersist(){
        if (getEnabled()==null) setEnabled(Boolean.TRUE);
        if (getDeleted()==null) setDeleted(Boolean.FALSE);
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 医生名称 */
    @Column(name = "name")
    private String name;

    /** 医生编号 */
    @Column(name = "code")
    private String code;

    /** 所属院区 */
    @Column(name = "branch")
    private String branch;

    /** 启用推送 */
    @Column(name = "enabled")
    private Boolean enabled;

    /** 删除标识 */
    @Column(name = "deleted")
    private Boolean deleted;

    /** 三病推送记录 */
//    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "icPushDoctors")
//    private Set<IcPushRecord> icPushRecords;

    /** 三病推送 - 科室 */
//    @ManyToOne(fetch = FetchType.LAZY)
//    private IcPushDepartment icPushDepartment;

// --- @Deprecated below -----------------------------------------------------------------------------------------------

    /** 科室编号 */
    @Deprecated
    @Transient
    @Column(name = "dept_code")
    private String deptCode;

    /** 院区管理员 */
    @Deprecated
    @Transient
    @Column(name = "hospital_admin")
    private Boolean hospitalAdmin;

    /** 是否启用推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push")
    private Boolean enableIcPush;

    /** 三病模板 */
    @Deprecated
    @Transient
    @ManyToOne
    private IcPushTemplate icPushTemplate;

    /** 是否启用三病推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_template")
    private Boolean enableIcPushTemplate;

    /** 是否启用管理员hiv推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_hiv")
    private Boolean enableIcPushHiv;

    /** 是否启用管理员hbv推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_hbv")
    private Boolean enableIcPushHbv;

    /** 是否启用管理员hbvDna推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_hbv_dna")
    private Boolean enableIcPushHbvDna;

    /** 是否启用管理员梅毒推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_syphilis")
    private Boolean enableIcPushSyphilis;


}
