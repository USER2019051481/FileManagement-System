package cn.attackme.myuploader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalculateConfig {
    public static String factor;

    @Value("${calculate.value}")
    public void setFactor(String factor) {
        CalculateConfig.factor = factor;
    }
}
