package cn.attackme.myuploader.service;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.File;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


/**
 * 文件上传服务
 */

public interface FileService {
    /**
    * 上传文件
    * @param fileDTO 文件DTO
    */
    public void upload(FileDTO fileDTO) ;

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
