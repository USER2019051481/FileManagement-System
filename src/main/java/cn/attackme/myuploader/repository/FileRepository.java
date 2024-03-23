package cn.attackme.myuploader.repository;
import cn.attackme.myuploader.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;


public interface FileRepository extends JpaRepository<File, Long>{
    @Modifying
    @Query(value = "UPDATE File f SET f.extractKeys_data = :extractKeysData WHERE f.name = :name")
    int updateExtractKeysDataByName(@Param("name") String name, @Param("extractKeysData") String extractKeysData);

    File findByName(String name);

    File findByMd5(String md5);

    // 注意：以下两个方法的方法名需要按照规范进行命名
    File getByName(String name);

    File getById(BigInteger id);

    void deleteById(Long id);

    File save(File file);
}
