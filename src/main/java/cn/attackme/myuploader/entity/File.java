package cn.attackme.myuploader.entity;

import lombok.Getter;
import lombok.Setter;




import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file")
@Getter
@Setter
public class File {


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



//    create table file(
//        id bigint auto_increment ,
//        name varchar(100) not null ,
//        md5 varchar(32) ,
//        path varchar(100) not null ,
//        upload_time datetime(3) not null ,
//        primary key (id)
//        );