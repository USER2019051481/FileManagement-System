package cn.attackme.myuploader.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class FileUtil {
    public void ifExist(File uploadedFile, String directoryPath) throws IOException {
        String uploadedFileName = uploadedFile.getName();
        String uploadedBaseName = uploadedFileName.substring(0, uploadedFileName.lastIndexOf('.')); // 去掉上传文件的扩展名

        File existingFile = new File(directoryPath, uploadedBaseName + "_*.*"); // 使用通配符匹配现有文件
        if (existingFile.exists()) {
            if (existingFile.isFile()) {
                // 获取现有文件名并去掉时间戳和后缀
                String existingFileName = existingFile.getName();
                String existingBaseName = existingFileName.substring(0, existingFileName.indexOf('_'));

                if (uploadedBaseName.equals(existingBaseName)) {
                    // 文件名匹配 (去掉时间戳和后缀后相同)，移动文件
                    File newFolder = new File(directoryPath, uploadedBaseName + "_old");
                    if (!newFolder.exists()) {
                        newFolder.mkdir();
                    }
                    Files.move(existingFile.toPath(), newFolder.toPath().resolve(existingFileName));
                }
            } else if (existingFile.isDirectory()) {
            }
        }
}
}
