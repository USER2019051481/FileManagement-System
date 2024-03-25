package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.File;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.utils.FileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileUtils fileUtils;

    public void upload(FileDTO fileDTO) {
        try {
            fileUtils.checkFileDuplicate(fileDTO.getName(), fileDTO.getMd5());
            File file = convertToEntity(fileDTO);
            fileRepository.save(file);
        } catch (FileDuplicateException e) {
            throw e;
        }
    }



    public File convertToEntity(FileDTO fileDTO) {
        File file = new File();
        file.setName(fileDTO.getName());
        file.setMd5(fileDTO.getMd5());
        file.setPath(fileDTO.getPath());
        file.setUpload_time(new Date());
        file.setExtractKeys_data(fileDTO.getExtractKeysData());
        return file;
    }

    public FileDTO convertToDTO(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        FileDTO fileDTO = new FileDTO();
        String path = UploadConfig.path + file.getOriginalFilename();
        fileDTO.setName(file.getOriginalFilename());
        fileDTO.setPath(path);
        fileDTO.setMd5(FileUtils.write(path, file.getInputStream()));
        fileDTO.setUploadTime(new Date());
        fileDTO.setExtractKeysData("");
        return fileDTO;
    }
}
