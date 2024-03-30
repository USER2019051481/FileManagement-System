package cn.attackme.myuploader.service;

import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;


/**
 * 文件上传服务
 */

public interface FileService {
    /**
    * 上传文件
    * @param fileDTO 文件DTO
    */
    public void upload(FileDTO fileDTO) ;

    public Map<String, List<String>> deleteFiles(List<String> names);

    /**
     * DTO转为entity
     * @param fileDTO
     * @return
     */
     File convertToEntity(FileDTO fileDTO) ;

    /**
     * file转为FileDTO
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    FileDTO convertToDTO(MultipartFile file) throws IOException, NoSuchAlgorithmException;

}
