package cn.attackme.myuploader.repository;

import cn.attackme.myuploader.entity.PropertyMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyMapRespository  extends JpaRepository<PropertyMapEntity, Long> {
    PropertyMapEntity save(PropertyMapEntity propertyMapEntity) ;
}
