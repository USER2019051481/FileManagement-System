package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.TokenFilter;
import cn.attackme.myuploader.utils.JsonUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Authentication")
@Api(tags = "Authentication", description = "验证所属医院")
public class AuthenticationController {
    @Autowired
    JsonUtil jsonUtil;

    @Autowired
    private TokenFilter tokenFilter;

    @PostMapping
    @ApiOperation(value ="auth by password" ,notes = "验证成功后会返回一个令牌(Token)，之后访问的所有API接口都需要在请求的头部(header)中携带这个令牌(Token)")
    public ResponseEntity<?> login(@RequestBody String password) throws Exception {
        String hospitalName = jsonUtil.getHospitalName(password);
        if (hospitalName != null) {
            String token = tokenFilter.generateToken(hospitalName);
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }
}
