package cn.attackme.myuploader.service;

import cn.attackme.myuploader.dto.FileDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;


/**
 * 文件上传服务
 */

public interface FileService {

    public String upload(MultipartFile[] files, String hospitalName)throws Exception;

    public String queryFiles(String hospital) throws JsonProcessingException;

    public String deleteFiles(String fileData, String hospital) throws JsonProcessingException;

    /**
     * MultipartFile转为FileDTO
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    FileDTO convertToDTO(MultipartFile file, String hospitalName) throws IOException, NoSuchAlgorithmException;

    /**
     * 判断是否存在冲突，并返回冲突行
     *
     * @param files 上传文件
     * @param conflictLines 冲突行
     * @return 是否存在冲突
     */
    boolean isConflict(MultipartFile[] files, List<String> conflictLines) throws IOException;

    File getFileByName(String fileName);

}
