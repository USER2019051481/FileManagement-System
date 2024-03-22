package cn.attackme.myuploader.dao;

import cn.attackme.myuploader.model.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileDao {
    /**
     * 更新文件的 data 字段
     *
     * @param name 文件名
     * @param extractKeys_data 要更新的数据
     * @return
     */
    int updateDataByName(String name, String extractKeys_data);

    /**
     * 通过主键获取一行数据
     * @return
     */
    File getByName(String name);
    /**
     * 通过主键获取一行数据
     * @return
     */
    File getById(Long id);

    /**
     * 插入一行数据
     * @param file
     * @return
     */
    int save(File file);

    /**
     * 更新一行数据
     * @param file
     * @return
     */
    int update(File file);

    /**
     * 删除一行数据
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 根据一个或多个属性获取File
     * @param file
     * @return
     */
    File getByFile(File file);
}
