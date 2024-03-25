package cn.attackme.myuploader.service;

public interface AnnotationService {
    /**
     *
     * @param annotationNamesString 批注值
     * @param name 文件名
     */
//    根据文件名，将评注值存入数据库
    void saveAnnotationNames(String annotationNamesString, String name);
}
