package cn.attackme.myuploader.service;

public interface AnnotationService {
//    根据文件名，将评注值存入数据库
    void saveAnnotationNames(String annotationNamesString, String name);
}
