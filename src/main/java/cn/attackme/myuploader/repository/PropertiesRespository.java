package cn.attackme.myuploader.repository;

import cn.attackme.myuploader.entity.FileEntity;
import cn.attackme.myuploader.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesRespository  extends JpaRepository<PropertyEntity, Long> {
    PropertyEntity save(PropertyEntity propertyEntity) ;

    PropertyEntity findByClassName(String className);
}
