package cn.attackme.myuploader.repository;

import cn.attackme.myuploader.entity.FileEntity;
import cn.attackme.myuploader.entity.PropertyEntity;
import cn.attackme.myuploader.entity.PropertyMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesRespository  extends JpaRepository<PropertyEntity, Long> {
    // 存储PropertyEntity
    PropertyEntity save(PropertyEntity propertyEntity) ;

    // 通过ClassName查找PropertyEntity
    PropertyEntity findByClassName(String className);

    // 通过ClassName和最新的date查找PropertyEntity全部信息
    PropertyEntity findFirstByClassNameOrderByDateDesc(String ClassName) ;
}
