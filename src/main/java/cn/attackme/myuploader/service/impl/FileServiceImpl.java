package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.FileEntity;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.service.FileService;
import cn.attackme.myuploader.service.Mapper.FileMapper;

import cn.attackme.myuploader.utils.HFileUtils;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;

import cn.attackme.myuploader.utils.exception.FileNotFoundException;
import cn.attackme.myuploader.utils.exception.FileSizeExceededException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;


@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private HFileUtils fileUtils;
    @Resource
    private FileMapper fileMapper ;
    @Autowired
    private FileService fileService ;


    @Value("${upload.path}")
    private String uploadPath;
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;

    public String upload(MultipartFile[] files, String hospitalName) throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        long totalFileSize = 0;
        try {
            fileUtils.createUploadDirectory(UploadConfig.path);
            for (MultipartFile file : files) {
                if (file.getSize() > maxFileSize.toBytes()) {
                    jsonObject.put("name", file.getOriginalFilename());
                    jsonObject.put("error", "文件过大");
                    jsonArray.add(jsonObject);
                }

                totalFileSize += file.getSize();
                if (totalFileSize > maxRequestSize.toBytes()) {
                    throw new FileSizeExceededException("上传文件总大小超过最大限制");
                }

                FileDTO fileDTO = convertToDTO(file, hospitalName);

                if (fileRepository.findByNameAndHospital(fileDTO.getName(), fileDTO.getHospital()) != null) {
                    jsonObject.put("name", file.getOriginalFilename());
                    jsonObject.put("error", "文件名重复");
                    jsonArray.add(jsonObject);
                } else {
                    FileEntity fileEntity = fileMapper.INSTANCT.dto2entity(fileDTO);
                    fileRepository.save(fileEntity);
                }
            }
        } catch (FileSizeExceededException e) {
            jsonObject.put("name", "null");
            jsonObject.put("error", "剩余文件无法上传");
            jsonArray.add(jsonObject);
            String jsonString = jsonArray.toString();
            return jsonString ;
        }
        String jsonString = jsonArray.toString();
        return jsonString;
    }

    public String queryFiles(String hospital) throws JsonProcessingException {
        List<FileEntity> files = fileRepository.findAllByHospital(hospital);
        Map<String, String> fileMap = new HashMap<>();
        for (FileEntity file : files) {
            fileMap.put("name",file.getName());
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(fileMap);

        return jsonString;
    }

    public String deleteFiles(String fileData, String hospital) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JsonNode rootNode = mapper.readTree(fileData);
        for (JsonNode node : rootNode) {
            String name = node.get("names").textValue();
            boolean delete1 = fileUtils.deleteDatabaseFile(name, hospital);
            boolean delete2 = fileUtils.deleteLocalFile(name);
            if (!delete1 || !delete2){
                jsonObject.put("name", name);
                jsonObject.put("error", "文件不存在");
            }
        }
        jsonArray.add(jsonObject);
        String jsonString = jsonArray.toString();
        return jsonString;
    }

    public FileDTO convertToDTO(MultipartFile file, String hospitalName) throws IOException, NoSuchAlgorithmException {
        FileDTO fileDTO = new FileDTO();
        String path = uploadPath + file.getOriginalFilename();
        fileDTO.setName(file.getOriginalFilename());
        fileDTO.setPath(path);
        fileDTO.setMd5(HFileUtils.write(path, file.getInputStream()));
        fileDTO.setUploadTime(new Date());
        fileDTO.setExtractKeysData("");
        fileDTO.setHospital(hospitalName);
        return fileDTO;
    }

    @Override
    public boolean isConflict(MultipartFile[] files, List<String> conflictLines) throws IOException {
        boolean flag = false;

        for (MultipartFile file : files) {
            // 上传文件的名字
            String uploadedFileName = file.getOriginalFilename();
            // 找到服务端相对应的文件
            File existingFile = fileService.getFileByName(uploadedFileName);

            if (existingFile == null) {
                log.debug("服务端不存在相应文件。");
                continue;
            }

              FileInputStream fis = new FileInputStream(existingFile);
                 // 使用 Apache POI 的 HWPF 模块加载 '.doc' 文件，并读取其内容
              HWPFDocument document = new HWPFDocument(fis) ;
                Range range = document.getRange();
                int numParagraphs = range.numParagraphs();
                Range commentsRange = document.getCommentsRange();
                int numComments = commentsRange.numParagraphs();

                // 逐段比较文档内容
                for (int i = 0; i < numParagraphs; i++) {
                    Paragraph paragraph = range.getParagraph(i);
                    String text = paragraph.text();


                    // 比较上传文件和服务端文件的段落内容
                    if (!isParagraphEqual(file, text,i )) {
                        conflictLines.add("冲突文件名 " + uploadedFileName + ", 段落: " + (i + 1) + " 服务端:： " + text);
                        flag = true; // 标记存在冲突
                    }
                }

                // 逐段比较批注值内容
                for(int i = 0 ; i < numComments ; i++){
                    Paragraph commentsRangeParagraph = commentsRange.getParagraph(i);
                    String CommentText = commentsRangeParagraph.text() ;

                    //比较上传的文件批注和服务端文件的段落内容
                    if(!isCommentsParagraphEqual(file,CommentText,i)){
                        conflictLines.add("冲突文件名： " + uploadedFileName + ", 段落 " + (i + 1) + "[批注值]: " + CommentText);
                        flag = true; // 标记存在冲突
                    }
                }
            }

        return flag;
    }

    // 比较上传文件批注值和服务端文件的段落内容是否相同
    private boolean isCommentsParagraphEqual(MultipartFile file, String commentText,int i) throws IOException {
        try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {

            Range range = document.getCommentsRange();

            if (i >= range.numParagraphs()) {
                // 处理索引越界的情况
                return false;
            }

            Paragraph rangeParagraph = range.getParagraph(i);
            String text = rangeParagraph.text() ;
            // 有内容相同，不存在冲突
            if (text.equals(commentText)) {
                return true;
            }


        }
        return false ;

    }

    // 比较上传文件和服务端文件的段落内容是否相同
    private boolean isParagraphEqual(MultipartFile file, String serverParagraph ,int i) throws IOException {
        try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {
            Range range = document.getRange();
            int numParagraphs = range.numParagraphs();
            if(numParagraphs <= i){
               // 超出对比范围,存在冲突
               return false;
           }
            Paragraph paragraph = range.getParagraph(i);
            String text = paragraph.text();

            // 有内容相同，不存在冲突
            if (text.equals(serverParagraph)) {
                return true;
            }

        }
        // 如果发现不一致，则表示存在冲突
        return false;
    }


    @Override
    public File getFileByName(String fileName) {
        // 根据名字找到服务端文件
        String filePath = uploadPath+ fileName ;
        // 检查文件是否存在
        File file =  new File(filePath);
        if(file.exists()){
            return file ;
        }
        log.debug(fileName+"文件不存在于服务端。");
        return null;
    }

}
