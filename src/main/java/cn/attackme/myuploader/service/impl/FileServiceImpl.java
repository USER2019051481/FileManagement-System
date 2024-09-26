package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.service.FileService;

import cn.attackme.myuploader.utils.exception.FileSizeExceededException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import lombok.var;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Autowired
    private FileService fileService ;

    @Value("${upload.path}")
    private String uploadPath;
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;

    @Override
    public String uploadFiles(MultipartFile[] files, String hospital) throws Exception {
        JSONArray jsonArray = new JSONArray();
        long totalFileSize = 0;

        String directoryPath = uploadPath + "/" + hospital;
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("文件夹创建失败: " + directoryPath);
            }
        }
        try {
            for (MultipartFile file : files) {
                if (file.getSize() > maxFileSize.toBytes()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", file.getOriginalFilename());
                    jsonObject.put("error", "文件过大");
                    jsonArray.add(jsonObject);
                    continue;
                }
                totalFileSize += file.getSize();
                if (totalFileSize > maxRequestSize.toBytes()) {
                    throw new FileSizeExceededException("上传文件总大小超过最大限制");
                }
                // 获取原始文件名
                String originalFilename = file.getOriginalFilename();
                // 规范化文件名，获取不带路径的文件名
                String fileName = FilenameUtils.getName(originalFilename);
                if(Files.exists(Paths.get(directoryPath, fileName))) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", fileName);
                    jsonObject.put("error", "文件名重复");
                    jsonArray.add(jsonObject);
                    continue;
                }
                Path filePath = Paths.get(directoryPath, fileName);
                var outputStream = Files.newOutputStream(filePath);
                outputStream.write(file.getBytes());
            }
        } catch (FileSizeExceededException e) {
            return e.getMessage();
        }
        return jsonArray.toString();
    }

    @Override
    public String queryFiles(String hospital) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray();
        String directoryPath = uploadPath + "/" + hospital;
        if (Files.exists(Paths.get(directoryPath))) {
            try {
                // 检查文件夹是否为空
                if (Files.list(Paths.get(directoryPath)).findAny().isPresent()) {
                    // 遍历文件夹内的所有文件
                    Files.walk(Paths.get(directoryPath))
                            .filter(Files::isRegularFile)
                            .forEach(filePath -> {
                                String fileName = filePath.getFileName().toString();
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name", fileName);
                                jsonArray.add(jsonObject);
                            });
                }
                // 如果文件夹为空，直接返回空数组的 JSON 字符串
            } catch (IOException e) {
                return e.getMessage();
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(jsonArray);
        return jsonString;
    }

    @Override
    public String deleteFiles(String fileData, String hospital) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JSONArray jsonArray = new JSONArray();
        JsonNode rootNode = mapper.readTree(fileData);
        for (JsonNode node : rootNode) {
            String name = node.get("name").textValue();
            String filePathString = uploadPath + "/" + hospital + "/" + name;
            Path filePath = Paths.get(filePathString);
            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("error", e.toString());
                    jsonObject.put("name", name);
                    jsonArray.add(jsonObject);
                    continue; // 处理下一个文件
                }
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("error", "文件不存在");
                jsonObject.put("name", name);
                jsonArray.add(jsonObject);
            }
        }
        String jsonString = jsonArray.toString();
        return jsonString;
    }

    @Override
    public String modifyFiles(MultipartFile file, String newName, String hospital) throws IOException {
        String name = file.getOriginalFilename();
        Path currentFilePath = Paths.get(uploadPath + "/" + hospital + "/" + name);

        if (!Files.exists(currentFilePath)) {
            return "文件未找到";
        }
        try {
            if (newName != null && !newName.isEmpty()) {
                Path newFilePath = Paths.get(uploadPath + "/" + hospital + "/" + newName);
                if (Files.exists(newFilePath)) {
                    return "文件名重复";
                }
                Files.copy(file.getInputStream(), currentFilePath, StandardCopyOption.REPLACE_EXISTING);
                Files.move(currentFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(file.getInputStream(), currentFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "文件修改成功";
        } catch (IOException e) {
            return "文件修改失败: " + e.getMessage();
        }
    }


    @Override
    public boolean isConflict(MultipartFile[] files, List<String> conflictLines, String hospital) throws IOException {
        boolean flag = false;

        for (MultipartFile file : files) {
            // 上传文件的名字
            String uploadedFileName = file.getOriginalFilename();
            // 找到服务端相对应的文件
            File existingFile = fileService.getFileByName(uploadedFileName, hospital);

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
    public File getFileByName(String fileName, String hospital) {
        // 根据名字找到服务端文件
        String filePath= uploadPath + "/" + hospital + "/" + fileName;
        // 检查文件是否存在
        File file =  new File(filePath);
        if(file.exists()){
            return file ;
        }
        log.debug(fileName+"文件不存在于服务端。");
        return null;
    }

}
