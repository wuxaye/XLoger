package com.xaye.loglibrary.format;

import com.xaye.loglibrary.utils.LoggerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author xaye
 *
 * @date: 2024/11/11
 */
public class JsonLogFormatter implements LogFormatter<String> {

    // 美化输出时使用的缩进空格数
    private static final int JSON_INDENT = 4;

    /**
     * 格式化 JSON 消息
     * @param message 要格式化的消息（JSON 字符串）
     * @return 格式化后的 JSON 字符串，如果格式化失败，则返回原始消息
     */
    @Override
    public String formatMessage(String message) {
        // 检查 JSON 消息是否为空
        if (message == null || message.trim().isEmpty()) {
            LoggerUtils.warn("JSON is empty.");  // 如果 JSON 为空或 null，记录警告日志
            return "";  // 返回空字符串
        }

        // 去除字符串两端的空格，避免多余的空格影响解析
        message = message.trim();

        // 判断 JSON 是对象还是数组，并分别处理
        if (message.startsWith("{")) {
            return formatJsonObject(message);  // 格式化 JSON 对象
        } else if (message.startsWith("[")) {
            return formatJsonArray(message);  // 格式化 JSON 数组
        } else {
            // 如果 JSON 格式无效（既不是对象也不是数组），记录警告日志
            LoggerUtils.warn("Invalid JSON format. JSON should start with '{' or '['.");
            return message;  // 如果 JSON 格式无效，直接返回原始消息
        }
    }

    /**
     * 格式化 JSON 对象，进行漂亮打印
     * @param json JSON 对象字符串
     * @return 格式化后的 JSON 字符串，如果格式化失败，返回原始字符串
     */
    private String formatJsonObject(String json) {
        try {
            // 使用 JSONObject 解析并格式化 JSON 对象
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString(JSON_INDENT);  // 格式化为带缩进的 JSON 字符串
        } catch (JSONException e) {
            // 如果格式化过程中发生异常，记录错误日志
            LoggerUtils.error("Error formatting JSON Object", e);
            return json;  // 如果格式化失败，返回原始 JSON 字符串
        }
    }

    /**
     * 格式化 JSON 数组，进行漂亮打印
     * @param json JSON 数组字符串
     * @return 格式化后的 JSON 字符串，如果格式化失败，返回原始字符串
     */
    private String formatJsonArray(String json) {
        try {
            // 使用 JSONArray 解析并格式化 JSON 数组
            JSONArray jsonArray = new JSONArray(json);
            return jsonArray.toString(JSON_INDENT);  // 格式化为带缩进的 JSON 字符串
        } catch (JSONException e) {
            // 如果格式化过程中发生异常，记录错误日志
            LoggerUtils.error("Error formatting JSON Array", e);
            return json;  // 如果格式化失败，返回原始 JSON 字符串
        }
    }
}
