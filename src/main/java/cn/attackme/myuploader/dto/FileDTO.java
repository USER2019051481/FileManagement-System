package cn.attackme.myuploader.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FileDTO {
    private String name;
    private String md5;
    private String path;
    private Date uploadTime;
    private String extractKeysData;
}
