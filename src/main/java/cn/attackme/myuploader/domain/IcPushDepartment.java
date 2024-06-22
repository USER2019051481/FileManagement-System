package cn.attackme.myuploader.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 三病推送 - 科室
 */
@Entity
@Table(name = "ic_push_department")
@Where(clause = "(deleted = false or deleted is null)")
@SQLDelete(sql = "UPDATE ic_push_department SET deleted=true WHERE id=?")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class IcPushDepartment implements Serializable {

    @PrePersist
    public void prePersist(){
        if (getEnabled()==null) setEnabled(Boolean.TRUE);
        if (getDeleted()==null) setDeleted(Boolean.FALSE);
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 科室名称 */
    @Column(name = "name")
    private String name;

    /** 科室编号 */
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
//    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "icPushDepartments")
//    private Set<IcPushRecord> icPushRecords;

    /** 三病推送 - 医生 */
//    @OneToMany(fetch = FetchType.LAZY)
//    private Set<IcPushDoctor> icPushDoctors;

// --- @Deprecated below -----------------------------------------------------------------------------------------------

    /** 是否启用科室推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push")
    private Boolean enableIcPush;

    /** 三病模板 */
    @Deprecated
    @Transient
    @Fetch(FetchMode.JOIN)
    @ManyToOne
    private IcPushTemplate icPushTemplate;

    /** 是否启用三病推送 */
    @Deprecated
    @Transient
    @Column(name = "enable_ic_push_template")
    private Boolean enableIcPushTemplate;

    /** 排序 */
    @Deprecated
    @Transient
    @Column(name = "sort")
    private Long sort;

}
