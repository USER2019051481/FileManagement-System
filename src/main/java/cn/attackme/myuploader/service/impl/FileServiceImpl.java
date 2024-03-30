package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.File;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.utils.FileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.exception.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    public Map<String, List<String>> deleteFiles(List<String> names) {
        List<String> successfullyDeletedFiles = new ArrayList<>();
        List<String> failedToDeleteFiles = new ArrayList<>();
        List<String> excepToDeleteFiles = new ArrayList<>();

        for (String name : names) {
            boolean databaseFileDeleted = false;
            boolean localFileDeleted = false;
            try {
               databaseFileDeleted = fileUtils.deleteDatabaseFile(name);
            } catch (FileNotFoundException e) {
                excepToDeleteFiles.add(e.getMessage());
            }

            try {
                localFileDeleted = fileUtils.deleteLocalFile(name);
            } catch (FileNotFoundException e) {
                excepToDeleteFiles.add(e.getMessage());
            }

            if (databaseFileDeleted || localFileDeleted) {
                successfullyDeletedFiles.add(name);
            } else {
                failedToDeleteFiles.add(name);
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("successfullyDeletedFiles", successfullyDeletedFiles);
        result.put("failedToDeleteFiles", failedToDeleteFiles);
        result.put("存在的特殊情况", excepToDeleteFiles);

        return result;
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
