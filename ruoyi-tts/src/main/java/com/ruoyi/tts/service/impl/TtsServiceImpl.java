package com.ruoyi.tts.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.tts.service.TtsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class TtsServiceImpl implements TtsService {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JSONObject serviceAuth(String username, String password) {
        String url = "https://account.xiaomi.com/pass/serviceLoginAuth2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("_json", true);
        body.add("sid", "micoapi");
        body.add("user", username);
        body.add("hash", DigestUtil.md5Hex(password).toUpperCase());
        body.add("callback", "https://api.mina.mi.com/sts");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        String result = restTemplate.postForObject(url, request, String.class);
        if (result == null) {
            throw new ServiceException("错误");
        }
        result = result.replace("&&&START&&&", "");
        return JSON.parseObject(result);
    }

    @Override
    public String loginMiAi(JSONObject authInfo) {
        String nonce = authInfo.getString("nonce");
        String ssecurity = authInfo.getString("ssecurity");
        String clientSign = genClientSign(nonce, ssecurity);

        String location = authInfo.getString("location");
        return location + "&clientSign=" + clientSign;
    }

    private String genClientSign(String nonce, String ssecurity) {
        String str = StrUtil.format("nonce={}&{}", nonce, ssecurity);
        String hashStr = Base64.encode(DigestUtil.sha1(str));
        return URLEncoder.createAll().encode(hashStr, StandardCharsets.UTF_8);
    }
}
