package com.xaye.loglibrary.format;

import com.xaye.loglibrary.utils.LoggerUtils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Author xaye
 *
 * @date: 2024/11/11
 */
public class XmlLogFormatter implements LogFormatter<String> {

    // 美化输出时使用的缩进空格数
    private static final int XML_INDENT = 4;

    /**
     * 格式化 XML 消息
     * @param message 要格式化的 XML 字符串
     * @return 格式化后的 XML 字符串，如果格式化失败，则返回原始消息
     */
    @Override
    public String formatMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            LoggerUtils.warn("XML is empty.");
            return "";
        }

        // 去除字符串两端的空格，避免多余的空格影响解析
        message = message.trim();

        // 格式化 XML 字符串
        return formatXml(message);
    }

    /**
     * 格式化 XML 字符串，进行漂亮打印
     * @param xml 要格式化的 XML 字符串
     * @return 格式化后的 XML 字符串，如果格式化失败，返回原始字符串
     */
    private String formatXml(String xml) {
        String formattedString = null;
        try {
            // 使用 javax.xml.transform 库来格式化 XML 字符串
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());

            // 创建 Transformer 实例
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // 设置输出格式的属性，包括缩进
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(XML_INDENT));

            // 执行转换并格式化 XML
            transformer.transform(xmlInput, xmlOutput);

            // 获取格式化后的 XML 字符串
            formattedString = xmlOutput.getWriter().toString().replaceFirst(">", ">" + System.lineSeparator());
        } catch (Exception e) {
            // 格式化失败时记录错误日志
            LoggerUtils.error("Error formatting XML", e);
            return xml;  // 如果格式化失败，返回原始 XML 字符串
        }
        return formattedString;
    }
}
