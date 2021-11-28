package com.actionworks.flashsale.app.auth;

import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.app.auth.model.ResourceEnum;

public interface AuthorizationService {
    AuthResult auth(String encryptedToken, ResourceEnum resourceEnum);

    AuthResult auth(String encryptedToken);

    AuthResult auth(Long userId);

    AuthResult auth(Long userId, ResourceEnum resourceEnum);
}
