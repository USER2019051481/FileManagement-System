package cn.attackme.myuploader.utils;


import cn.attackme.myuploader.config.CalculateConfig;
import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.exception.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件操作工具类
 */
@Slf4j
@Component
public class HFileUtils {

    @Autowired
    private FileRepository fileRepository;

    public void createUploadDirectory(String uploadPath) {
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("创建上传文件夹 '" + uploadPath + "' 成功");
            } else {
                throw new RuntimeException(uploadPath+": 创建上传文件夹失败");
            }
        }
    }

    /**
     * 写入文件并计算MD5值
     * @param target
     * @param src
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String write(String target, InputStream src) throws IOException, NoSuchAlgorithmException {
        OutputStream os = new FileOutputStream(target);
        MessageDigest md = MessageDigest.getInstance(CalculateConfig.factor);
        byte[] buf = new byte[1024];
        int len;
        while (-1 != (len = src.read(buf))) {
            os.write(buf,0,len);
            md.update(buf, 0, len); // 更新MD5哈希值
        }

        os.flush();
        os.close();

        byte[] digest = md.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }


    public boolean deleteLocalFile(String name) {
        String filePath = UploadConfig.path + java.io.File.separator + name;
        java.io.File localFile = new java.io.File(filePath);
        if (localFile.exists()) {
            localFile.delete();
            return true;
        }
        return false;
    }

    @Transactional
    //删除数据库记录
    public boolean deleteDatabaseFile(String name, String hospital){
        if (fileRepository.findByNameAndHospital(name, hospital) != null) {
            fileRepository.deleteByName(name);
            return true;
        }
        return false;
    }
}
