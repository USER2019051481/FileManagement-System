package cn.attackme.myuploader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "md5")
    private String md5;

    @Column(name = "path")
    private String path;

    @Column(name = "upload_time")
    private Date upload_time;

    @Column(name = "extractKeys_data", columnDefinition = "LONGTEXT")
    private String extractKeys_data;



}

