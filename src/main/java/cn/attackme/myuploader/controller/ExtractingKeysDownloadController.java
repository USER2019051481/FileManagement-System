package cn.attackme.myuploader.controller;


import cn.attackme.myuploader.entity.File;
import cn.attackme.myuploader.repository.FileRepository;
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
    private FileRepository fileRepository ;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<String> downloadExtractingKeys(@PathVariable String filename) {
        File file = fileRepository.getByName(filename);

        System.out.println("file:"+file.getExtractKeys_data() +file.getId()+file.getUpload_time() );
        return  ResponseEntity.ok().body(file.getExtractKeys_data()) ;
    }
}
