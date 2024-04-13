package cn.attackme.myuploader.repository;
import cn.attackme.myuploader.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;


@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long>{
    @Modifying
    @Query(value = "UPDATE FileEntity f SET f.extractKeys_data = :extractKeysData WHERE f.name = :name")
    int updateExtractKeysDataByName(@Param("name") String name, @Param("extractKeysData") String extractKeysData);

    FileEntity findByNameAndHospital(String name, String hospital);

    List<FileEntity> findAllByHospital(String hospital);

    FileEntity findByName(String name);

    FileEntity findByMd5(String md5);

    // 注意：以下两个方法的方法名需要按照规范进行命名
    FileEntity getByName(String name);

    FileEntity getById(BigInteger id);

    void deleteById(Long id);
    void deleteByName(String name);

    FileEntity save(FileEntity file);
}
