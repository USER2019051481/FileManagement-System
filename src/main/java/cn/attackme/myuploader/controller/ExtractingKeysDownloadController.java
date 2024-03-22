package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.dao.FileDao;
import cn.attackme.myuploader.model.File;
import com.sun.javaws.IconUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/DownloadKeys")
public class ExtractingKeysDownloadController {

    @Autowired
    private FileDao fileDao ;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<String> downloadExtractingKeys(@PathVariable String filename) {
        File file = fileDao.getByName(filename);
        System.out.println("file:"+file.getExtractKeysData()+file.getId()+file.getUploadTime());
        return  ResponseEntity.ok().body(file.getExtractKeysData()) ;
    }
}
