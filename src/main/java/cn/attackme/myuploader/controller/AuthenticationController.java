package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.TokenFilter;
import cn.attackme.myuploader.utils.HospitalUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "Authentication", description = "验证所属医院")
public class AuthenticationController {
    @Autowired
    HospitalUtil hospitalUtil;

    @Autowired
    private TokenFilter tokenFilter;

    @PostMapping("/auth")
    @ApiOperation(value ="auth by password" ,notes = "验证成功后会返回一个令牌(Token)，之后访问的所有API接口都需要在请求的头部(header)中携带这个令牌(Token)")
    public ResponseEntity<?> auth(@RequestBody String authData) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(authData);
        String workid = jsonNode.get("workid").textValue();
        if (workid == null || workid.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("workid不能为空");
        }
        String password = jsonNode.get("password").textValue();
        try {
            String hospitalName = hospitalUtil.getHospitalName(password);
            String token = tokenFilter.generateToken(hospitalName, workid);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            if (e.getMessage().equals("密码错误")) {
                return ResponseEntity.badRequest().body("密码错误");
            } else {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    }
}
