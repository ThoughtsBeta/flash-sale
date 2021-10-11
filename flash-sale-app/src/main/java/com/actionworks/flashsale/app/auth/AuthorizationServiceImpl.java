package com.actionworks.flashsale.app.auth;

import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.app.auth.model.ResourceEnum;
import com.actionworks.flashsale.app.auth.model.Token;
import com.actionworks.flashsale.controller.exception.AuthException;
import com.actionworks.flashsale.util.Base64Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static com.actionworks.flashsale.controller.exception.ErrorCode.INVALID_TOKEN;
import static com.actionworks.flashsale.controller.exception.ErrorCode.UNAUTHORIZED_ACCESS;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    public static void main(String[] args) throws Exception {
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File("/Users/Tao/Downloads/tokens.csv");
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println("token");
        for (long i = 1L; i < 50000L; i++) {
            String token = Base64Util.encode(JSONObject.toJSONString(new Token().setExpireDate("2021-12-04 17:57:54").setUserId(i)));
            pw.println(token);
        }
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对token的解析需要结合登录时的令牌加密逻辑；
     */
    @Override
    public AuthResult auth(String encryptedToken, ResourceEnum resourceId) {
        Token token = parseToken(encryptedToken);
        if (token == null) {
            throw new AuthException(INVALID_TOKEN);
        }
        if (!hasAccessAuthorized(token, resourceId)) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        return new AuthResult()
                .setUserId(token.getUserId())
                .pass();
    }

    @Override
    public AuthResult auth(String encryptedToken) {
        Token token = parseToken(encryptedToken);
        if (token == null) {
            throw new AuthException(INVALID_TOKEN);
        }
        return new AuthResult()
                .setUserId(token.getUserId())
                .pass();
    }

    /**
     * 解析令牌
     * Notice：演示需要，这里使用简单的编码，正式开发中应遵循严格的加解密规则
     *
     * @param encryptedToken 已加密的用户令牌
     * @return 解析后的用户令牌信息
     */
    private Token parseToken(String encryptedToken) {
        try {
            String parsedToken = Base64Util.decode(encryptedToken);
            return JSON.parseObject(parsedToken, Token.class);
        } catch (Exception e) {
            throw new AuthException(INVALID_TOKEN);
        }
    }

    /**
     * 是否已经获得授权访问目标资源
     * Notice：演示需要，这里直接返回true，正式开发中应该结合权限配置执行严格校验
     *
     * @param token      解析后的用户令牌信息
     * @param resourceId 资源标识
     * @return 授权鉴定结果
     */
    private boolean hasAccessAuthorized(Token token, ResourceEnum resourceId) {
        return true;
    }
}
