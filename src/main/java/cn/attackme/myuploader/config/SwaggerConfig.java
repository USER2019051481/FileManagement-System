package cn.attackme.myuploader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration  //表明这是一个注解类
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket getClientDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //分组名称
                .groupName("ClientApi")
                .select()
                //扫描的包名称
                .apis(RequestHandlerSelectors.basePackage("cn.attackme.myuploader.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("加载项文件管理-api接口文档")
                .description("powered by l&y")
                .version("1.0")
                .build();
    }
}
