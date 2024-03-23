package cn.attackme.myuploader.service;

import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.File;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


/**
 * 文件上传服务
 */
@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileUtils fileUtils;
    /**
     * 上传文件
     * @param fileDTO
     */
    public void upload(FileDTO fileDTO) throws IOException, NoSuchAlgorithmException {
        try {
            fileUtils.checkFileDuplicate(fileDTO.getName(), fileDTO.getMd5());
            File file = convertToEntity(fileDTO);
            fileRepository.save(file);
        } catch (FileDuplicateException e) {
            throw e;
        }
    }


    private File convertToEntity(FileDTO fileDTO) {
        File file = new File();
        file.setName(fileDTO.getName());
        file.setMd5(fileDTO.getMd5());
        file.setPath(fileDTO.getPath());
        file.setUpload_time(new Date());
        file.setExtractKeys_data(fileDTO.getExtractKeysData());
        return file;
    }

}
