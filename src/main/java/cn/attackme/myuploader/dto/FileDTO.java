package cn.attackme.myuploader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {
    private String name;
    private String md5;
    private String path;
    private Date uploadTime;
    private String extractKeysData;
    private String hospital;
}
