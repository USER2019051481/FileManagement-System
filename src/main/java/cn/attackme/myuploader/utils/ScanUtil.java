package cn.attackme.myuploader.utils;

import io.swagger.annotations.ApiModelProperty;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Column;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScanUtil {
    public static void scanAndStoreAnnotations(String packageName) {
        List<String> lines = new ArrayList<>();

        // 获取指定包下的所有类
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .setScanners(new SubTypesScanner(false)));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        // 遍历每个类
        for (Class<?> clazz : classes) {
            String className = "Class: " + clazz.getSimpleName();
            lines.add(className);
            // 获取类中声明的字段
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // 检查字段是否带有 ApiModelProperty 和 Column 注解
                if (field.isAnnotationPresent(ApiModelProperty.class) && field.isAnnotationPresent(Column.class)) {
                    // 获取注解的值
                    ApiModelProperty apiModelProperty = field.getDeclaredAnnotation(ApiModelProperty.class);
                    Column column = field.getDeclaredAnnotation(Column.class);
                    String annotationValue = "Field: " + field.getName() +   ", ApiModelProperty: " + apiModelProperty.value() +   ", Column: " + column.name();
                    lines.add(annotationValue);
                }
            }
            lines.add("");
        }
        String filePath = "D:\\annotations.txt";
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
