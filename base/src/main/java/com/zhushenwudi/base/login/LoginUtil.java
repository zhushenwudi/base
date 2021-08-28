package com.zhushenwudi.base.login;

import com.zhushenwudi.base.login.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginUtil {
    public static String getEnCodePwd(String key, String password) throws Exception {
        return Aes.aesEncrypt(password, getSecret(key));
//        return BtoAAtoB.btoa(encodeURIComponent(Aes.aesEncrypt(password, getSecret(key))));
    }

    private static String encodeURIComponent(String s) {
        if (s == null) {
            return null;
        }
        String result;
        try {
            result = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

    private static String getSecret(String username) {
        StringBuilder str_s = new StringBuilder(username);
        String base = Base64.encodeBase64String(username.getBytes()).trim();
        while (base.length() < 16) {
            str_s.append("_");
            base = Base64.encodeBase64String(str_s.toString().getBytes()).trim();
        }
        return base.substring(base.length() - 16);
    }
}
