package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.dao.FileDao;
import cn.attackme.myuploader.model.File;
import cn.attackme.myuploader.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnotationServiceImpl implements AnnotationService {
    @Autowired
    private FileDao fileDao ;
    @Override
    public void saveAnnotationNames(String annotationNamesString, String name) {
        int success = fileDao.updateDataByName(name,annotationNamesString) ;

    }
}
