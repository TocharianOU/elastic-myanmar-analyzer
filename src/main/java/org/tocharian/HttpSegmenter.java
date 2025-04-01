package org.tocharian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过HTTP调用外部分词服务
 */
public class HttpSegmenter {
    private static final Logger logger = Logger.getLogger(HttpSegmenter.class.getName());
    private final String serviceUrl;
    
    /**
     * 创建HTTP分词器
     * @param serviceUrl 分词服务URL，例如 "http://gis.tocharian.eu:5000/segment"
     */
    public HttpSegmenter(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        logger.info("初始化HTTP分词器，服务地址: " + serviceUrl);
    }
    
    /**
     * 对文本进行分词
     * @param text 待分词文本
     * @return 分词后的文本，以空格分隔
     */
    public String segmentText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 不使用SpecialPermission，仅使用标准Java安全API
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(serviceUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "text/plain");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    
                    String jsonInput = "{\"text\":\"" + escapeJson(text) + "\"}";
                    
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder response = new StringBuilder();
                        try (InputStream is = connection.getInputStream()) {
                            byte[] responseBytes = is.readAllBytes();
                            response.append(new String(responseBytes, StandardCharsets.UTF_8));
                        }
                        
                        String jsonResponse = response.toString();
                        Pattern pattern = Pattern.compile("\"segmented_text\"\\s*:\\s*\"(.*?)\"");
                        Matcher matcher = pattern.matcher(jsonResponse);
                        if (matcher.find()) {
                            String segmentedText = matcher.group(1);
                            segmentedText = decodeUnicode(segmentedText);
                            return segmentedText;
                        }
                        
                        logger.warning("无法从JSON响应中提取分词结果: " + jsonResponse);
                        return text;
                    } else {
                        logger.warning("分词服务返回错误代码: " + responseCode);
                        return text;
                    }
                    
                } catch (IOException e) {
                    logger.severe("分词请求失败: " + e.getMessage());
                    return text;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            logger.severe("权限异常: " + e.getMessage());
            return text;
        }
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 解码Unicode转义序列
     */
    private String decodeUnicode(String input) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("\\u", i)) {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int codePoint = Integer.parseInt(hex, 16);
                    result.append((char) codePoint);
                    i += 6;
                } catch (NumberFormatException e) {
                    result.append(input.charAt(i));
                    i++;
                }
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
} 