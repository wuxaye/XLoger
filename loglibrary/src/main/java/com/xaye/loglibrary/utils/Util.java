package com.xaye.loglibrary.utils;

/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class Util {
    public static String[] splitStr(int MAX_LENGTH,String str) {
        // 字符串长度
        int length = str.length();
        // 返回的数组
        String[] strs = new String[length / MAX_LENGTH + 1];
        int start = 0;
        for (int i = 0; i < strs.length; i++) {
            // 判断是否达到最大长度
            if (start + MAX_LENGTH < length) {
                strs[i] = str.substring(start, start + MAX_LENGTH);
                start += MAX_LENGTH;
            } else {
                strs[i] = str.substring(start, length);
                start = length;
            }
        }
        return strs;
    }

}
