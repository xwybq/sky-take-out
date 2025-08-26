package com.sky.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.sky.exception.AccountNotFoundException;

public class CookieUtils {

    /**
     * 从Cookie中解析user_info获取用户ID
     */
    public static Long getUserIdFromCookie(HttpServletRequest request) {
        // 1. 获取所有Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AccountNotFoundException("未获取到用户信息，请重新登录");
        }

        // 2. 查找名为"user_info"的Cookie
        String userInfoStr = null;
        for (Cookie cookie : cookies) {
            if ("user_info".equals(cookie.getName())) {
                userInfoStr = cookie.getValue();
                break;
            }
        }

        if (userInfoStr == null) {
            throw new AccountNotFoundException("用户信息不存在，请重新登录");
        }

        try {
            // 3. 解码URL编码的字符串（Cookie中的特殊字符会被URL编码，如%22对应"）
            String decodedUserInfo = java.net.URLDecoder.decode(userInfoStr, "UTF-8");

            // 4. 解析JSON获取id
            JSONObject userInfoJson = JSONObject.parseObject(decodedUserInfo);
            return userInfoJson.getLong("id"); // 注意：如果id是字符串类型，用getString()再转Long
        } catch (Exception e) {
            // 解析失败（如格式错误、缺少id字段）
            throw new AccountNotFoundException("用户信息解析失败，请重新登录");
        }
    }
}