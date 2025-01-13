package cn.attackme.myuploader.service.task;

import cn.attackme.myuploader.service.impl.PropertyParseServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class GetSwaggerTask {
    @Autowired
    private PropertyParseServiceImpl propertyParseService;

    @Value("${swagger.path}")
    private String swagger_URL ;

    private static Path swagger_Path = Paths.get("src/main/resources/swagger.json");
    @Scheduled(cron = "0 0 0 1 * ?") // 每月执行一次
    //@PostConstruct
    public void executeTask() {
        try {
            if(downloadSwaggerFile()){
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(Files.newInputStream(swagger_Path));
                propertyParseService.getClass(jsonNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean downloadSwaggerFile() throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(new URL(swagger_URL));
        JsonNode definitionsNode = jsonNode.get("definitions");
        if (definitionsNode != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(definitionsNode);
            String oldContent = new String(Files.readAllBytes(swagger_Path));
            if (!oldContent.equals(jsonContent)) {
                Files.write(swagger_Path, jsonContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            }
        }
        return false;
    }
}
