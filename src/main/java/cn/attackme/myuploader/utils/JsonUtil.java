package cn.attackme.myuploader.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JsonUtil {
        public Map<String, String> readJsonFile() throws Exception {
                ObjectMapper objectMapper = new ObjectMapper();

                // 使用类加载器加载资源文件
                InputStream inputStream = JsonUtil.class.getClassLoader().getResourceAsStream("hospitals.json");
                if (inputStream == null) {
                        throw new Exception("JSON file not found in resources directory");
                }

                List<Map<String, String>> hospitals = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, String>>>() {});

                // 将数据存入 Map
                Map<String, String> hospitalMap = new HashMap<>();
                for (Map<String, String> hospital : hospitals) {
                        String hospitalName = hospital.get("hospitalName");
                        String password = hospital.get("password");
                        hospitalMap.put(hospitalName, password);
                }

                // 输出 Map 中的内容（仅供参考）
                for (Map.Entry<String, String> entry : hospitalMap.entrySet()) {
                        System.out.println("Hospital Name: " + entry.getKey() + ", Password: " + entry.getValue());
                }

                return hospitalMap;
        }

        public String getHospitalName(String password) throws Exception {
                Map<String, String> hospitalCredentials = readJsonFile();
                for (Map.Entry<String, String> entry : hospitalCredentials.entrySet()) {
                        if (entry.getValue().equals(password)) {
                                return entry.getKey();
                        }
                }
                return null;
        }

//        private static String generateHS512Key() {
//                try {
//                        // 使用SecureRandom生成随机字节序列
//                        SecureRandom secureRandom = new SecureRandom();
//                        byte[] keyBytes = new byte[64];
//                        secureRandom.nextBytes(keyBytes);
//
//                        // 将字节序列转换为Base64编码的字符串
//                        return Base64.getEncoder().encodeToString(keyBytes);
//                } catch (Exception e) {
//                        e.printStackTrace();
//                        return null;
//                }
//        }
//
//        public static void main(String[] args) {
//                String hs512Key = generateHS512Key();
//                System.out.println("Generated HS512 Key: " + hs512Key);
//        }
}

