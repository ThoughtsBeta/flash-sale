package com.actionworks.flashsale.app.auth;

import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.app.auth.model.ResourceEnum;

public interface AuthorizationService {
    AuthResult auth(String encryptedToken, ResourceEnum flashItemCreate);

    AuthResult auth(String encryptedToken);
}
