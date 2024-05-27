package cn.attackme.myuploader.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidateService{

    private static final Pattern SIMPLE_BINDING_PATTERN = Pattern.compile("\\$\\[[^\\[\\]]+\\]\\{[^\\{\\}]+\\}");
    private static final Pattern CHECKBOX_PATTERN = Pattern.compile("@\\{(.+?)}");
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("<<if \\[(.*?)\\]>>(.*?)<</if>>", Pattern.DOTALL);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("<<image \\[(.+?)\\]>>(\\s1--\\d+--\\d+)?", Pattern.DOTALL);

    public String extractComments(MultipartFile file) throws IOException {
        // 从 MultipartFile 获取输入流
        InputStream inputStream = file.getInputStream();
        HWPFDocument document = new HWPFDocument(inputStream);

        Range commentsRange = document.getCommentsRange();
        int numComments = commentsRange.numParagraphs();
        StringBuilder commentsContent = new StringBuilder(); // 用于存储批注内容
        // 逐段获取批注内容
        for (int i = 0; i < numComments; i++) {
            Paragraph commentsRangeParagraph = commentsRange.getParagraph(i);
            String commentText = commentsRangeParagraph.text();
            // 将批注内容添加到 StringBuilder 中
            commentsContent.append(commentText);
        }
        // 关闭文件输入流
        inputStream.close();
        // 返回批注内容
        return commentsContent.toString();
    }

    public List<String> validate(MultipartFile[] files) {
        List<String> result = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String comments = extractComments(file);
                String[] commentsArray = comments.split("\\r");
                List<String> invalidComments = new ArrayList<>();
                result.addAll(checkConditionPattern(file));
                // 验证每条批注是否符合指定的模式
                for (String comment : commentsArray) {
                    if (isValidComment(comment) || checkTableDataBinding(comment)) {
                        invalidComments.add(comment);
                    }
                }
                // 构建结果列表
                if (!invalidComments.isEmpty() ) {
                    result.add("-- Invalid Comments:");
                    result.addAll(invalidComments);
                }
                else {
                    result.add("-- All comments are valid.");
                }
            } catch (IOException e) {
                result.add("Error reading or processing file: " + e.getMessage());
            }
        }
        return result;
    }

    private boolean isValidComment(String comment) {
        // 检查是否符合任意一个模式
        return SIMPLE_BINDING_PATTERN.matcher(comment).matches()
                || CHECKBOX_PATTERN.matcher(comment).matches()
                || CONDITIONAL_PATTERN.matcher(comment).matches()
                || IMAGE_PATTERN.matcher(comment).matches();
    }

    public boolean checkTableDataBinding(String text) {
        String pattern = "\\$\\[D\\]\\{(.+?)\\}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String tableDirective = m.group(1);
            if (tableDirective.contains("[idx]")) {
                int count = countOccurrences(tableDirective, "[idx]");
                return count <= 2;
            }
        }
        return false;
    }

    public  List<String> checkConditionPattern(MultipartFile file) throws IOException {
        // 从 MultipartFile 获取输入流
        InputStream inputStream = file.getInputStream();
        HWPFDocument document = new HWPFDocument(inputStream);

        Range range = document.getRange(); // 获取文档的范围
        String content = range.text(); // 获取文档的内容

        String patternString = "<<if\\s+([^>]+)>>"; // 匹配 <<if   >> 结构，并提取 if 后面的内容

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);

        List<String> invalidConditions = new ArrayList<>(); // 记录不符合正则表达式的条件

        while (matcher.find()) {
            String condition = matcher.group(); // 获取匹配到的整个 if 结构内容

            if (!condition.matches(CONDITIONAL_PATTERN.pattern())) {
                invalidConditions.add(condition);
            }
        }
        return invalidConditions;
    }

    public int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }
}