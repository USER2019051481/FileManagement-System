package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/DeleteFile")
@CrossOrigin
public class FileDeleteController {
    @Autowired
    private FileService fileService;

    @DeleteMapping
    public ResponseEntity<Map<String, List<String>>> deleteFiles(@RequestBody List<String> names) {
        Map<String, List<String>> result = fileService.deleteFiles(names);
        return ResponseEntity.ok(result);
    }
}
