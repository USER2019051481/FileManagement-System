package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.service.FileService;

import cn.attackme.myuploader.utils.exception.FileSizeExceededException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import lombok.var;
import org.apache.commons.io.FileUtils;
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

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        Path hospitalPath = Paths.get(uploadPath, hospital);
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

                String baseName = file.getOriginalFilename().split("\\.[^.]+$")[0]; // 去掉上传文件的扩展名
                String extension = FilenameUtils.getExtension(file.getOriginalFilename());

                // 检查是否有同名文件
                DirectoryStream.Filter<Path> filter = entry -> {
                    String entryName = entry.getFileName().toString();
                    return entryName.startsWith(baseName + "_") && entryName.endsWith("." + extension);
                };

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(hospitalPath, filter)) {
                    for (Path existingFile : stream) {
                        // 创建历史版本目录路径
                        String historyFolderName = baseName + "_old";
                        Path historyFolderPath = hospitalPath.resolve(historyFolderName);
                        // 检查历史版本目录是否存在，如果不存在则创建
                        if (!Files.exists(historyFolderPath)) {
                            Files.createDirectory(historyFolderPath);
                        }
                        Files.move(existingFile, historyFolderPath.resolve(existingFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // 添加时间戳并生成新文件名
                long timestampMillis = System.currentTimeMillis();
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); //  自定义格式
                String timestamp = localDateTime.format(formatter);
                String newFileName = baseName + "_" + timestamp + (extension.isEmpty() ? "" : "." + extension);
                Path filePath = Paths.get(directoryPath, newFileName);
                // 保存文件
                try (var outputStream = Files.newOutputStream(filePath)) {
                    outputStream.write(file.getBytes());
                }
            }
        } catch (FileSizeExceededException e) {
            return e.getMessage();
        } catch (IOException e) {
            throw new IOException("文件处理错误: " + e.getMessage(), e);
        }
        return jsonArray.toString();
    }

    @Override
    public String queryFiles(String hospital) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray();
        String directoryPath = uploadPath + "/" + hospital;
        if (Files.exists(Paths.get(directoryPath))) {
            try {
                Files.list(Paths.get(directoryPath))
                        .forEach(filePath -> {
                            String fileName = filePath.getFileName().toString();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name", fileName);
                            jsonObject.put("type", Files.isDirectory(filePath) ? "directory" : "file"); // 添加类型字段
                            jsonArray.add(jsonObject);
                        });
            } catch (IOException e) {
                return e.getMessage();
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(jsonArray);
        return jsonString;
    }

    public String queryOldFiles(String fileName, String hospital) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray();
        String baseName = fileName.split("_")[0]; // 取第一个部分作为基本名称
        String folderName = baseName + "_old";
        String directoryPath = uploadPath + "/" + hospital + "/" + folderName;
        File folder = new File(directoryPath);
        try {
            if (!folder.exists() || !folder.isDirectory()) {
                throw new NotFoundException("文件夹不存在");
            }
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", file.getName());
                    jsonArray.add(jsonObject);
                }
            }
        } catch (Exception e) {
            return e.getMessage();
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

    public String deleteOldFiles(String dirname,String fileData, String hospital) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(fileData);
        Path hospitalPath = Paths.get(uploadPath, hospital);
        JSONArray jsonArray = new JSONArray();
        Path subfolderPath = hospitalPath.resolve(dirname);
        if (Files.exists(subfolderPath) && Files.isDirectory(subfolderPath)) {
            for (JsonNode node : rootNode) {
                String filename = node.get("name").textValue();
                Path filePath = subfolderPath.resolve(filename);;
                // 检查文件是否存在并尝试删除
                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath);
                    } catch (IOException e) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("error", e.toString());
                        jsonObject.put("name", filename);
                        jsonArray.add(jsonObject);
                    }
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("error", "文件不存在");
                    jsonObject.put("name", filename);
                    jsonArray.add(jsonObject);
                }
            }
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("error", "子文件夹不存在");
            jsonObject.put("name", dirname);
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    public void deleteOldDirectory(String name, String hospital) throws IOException {
        Path hospitalPath = Paths.get(uploadPath, hospital);
        String subfolderName = name;
        Path subfolderPath = hospitalPath.resolve(subfolderName);
        // 删除目录本身
        if (Files.exists(subfolderPath) && Files.isDirectory(subfolderPath)) {
            // 使用Files.walkFileTree来递归删除文件和子目录
            Files.walkFileTree(subfolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw exc; // 重新抛出异常，以便外部捕获处理
                }
            });
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
