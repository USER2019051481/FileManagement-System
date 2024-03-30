package cn.attackme.myuploader.utils;


import cn.attackme.myuploader.config.CalculateConfig;
import cn.attackme.myuploader.config.UploadConfig;
import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.utils.exception.FileDuplicateException;
import cn.attackme.myuploader.utils.exception.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件操作工具类
 */
@Component
public class FileUtils {

    @Autowired
    private FileRepository fileRepository;
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

    //检查文件是否重复上传
    public void checkFileDuplicate(String name, String md5) {
        if (fileRepository.findByMd5(md5) != null) {
            throw new FileDuplicateException("文件已存在: " + fileRepository.findByMd5(md5).getName());
        }

        if (fileRepository.findByName(name) != null) {
            throw new FileDuplicateException("文件名重复: "+ name);
        }
    }

    //检查文件大小
    public void checkFileSize(MultipartFile file, DataSize maxSize) {
        if (file.getSize() > maxSize.toBytes()) {
            throw new FileDuplicateException("文件大小超过限制");
        }
    }

    public boolean deleteLocalFile(String name) {
        String filePath = UploadConfig.path + java.io.File.separator + name;
        java.io.File localFile = new java.io.File(filePath);
        if (localFile.exists()) {
            return localFile.delete();
        } else {
            throw new FileNotFoundException("本地File '" + name + "' not found");
        }
    }

    @Transactional
    //删除数据库记录
    public boolean deleteDatabaseFile(String name){
        if (fileRepository.findByName(name) != null) {
            fileRepository.deleteByName(name);
            return true;
        } else {
            throw new FileNotFoundException("数据库中File '" + name + "' not found");
        }
    }

}
