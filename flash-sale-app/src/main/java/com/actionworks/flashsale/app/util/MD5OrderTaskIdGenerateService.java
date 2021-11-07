package com.actionworks.flashsale.app.util;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class MD5OrderTaskIdGenerateService implements OrderTaskIdGenerateService {
    @Override
    public String generatePlaceOrderTaskId(Long userId, Long itemId) {
        String toEncrypt = userId + "_" + itemId;
        return DigestUtils.md5DigestAsHex(toEncrypt.getBytes());
    }
}
