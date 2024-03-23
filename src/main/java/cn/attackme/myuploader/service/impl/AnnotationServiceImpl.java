package cn.attackme.myuploader.service.impl;

import cn.attackme.myuploader.repository.FileRepository;
import cn.attackme.myuploader.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AnnotationServiceImpl implements AnnotationService {
    @Autowired
    private FileRepository fileRepository;

    @Override
    public void saveAnnotationNames(String annotationNamesString, String name) {
        int success = fileRepository.updateExtractKeysDataByName(name,annotationNamesString) ;
    }
}
