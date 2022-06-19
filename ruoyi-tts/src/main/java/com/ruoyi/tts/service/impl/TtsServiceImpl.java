package com.ruoyi.tts.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.tts.dto.AuthInfo;
import com.ruoyi.tts.dto.Session;
import com.ruoyi.tts.dto.TtsResult;
import com.ruoyi.tts.service.TtsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TtsServiceImpl implements TtsService {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AuthInfo serviceAuth(String username, String password) {
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
        assert result != null;
        result = result.replace("&&&START&&&", "");

        log.info("登录结果：{}", JSON.parseObject(result, String.class));

        AuthInfo authInfo = JSON.parseObject(result, AuthInfo.class);
        if (authInfo.getCode() != 0) {
            throw new ServiceException(authInfo.getDescription());
        }
        return authInfo;
    }

    @Override
    public Session loginMiAi(AuthInfo authInfo) {
        String clientSign = genClientSign(authInfo.getNonce(), authInfo.getSsecurity());
        String url = authInfo.getLocation() + "&clientSign=" + clientSign;

        ResponseEntity<String> response = restTemplate.getForEntity(URI.create(url), String.class);
        List<String> cookieList = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        return new Session().setCookieList(cookieList);
    }

    @Override
    public JSONArray getDevice(Session session) {
        String url = "https://api.mina.mi.com/admin/v2/device_list";

        HttpEntity<Object> request = new HttpEntity<>(null, getHeaders(session));
        ResponseEntity<TtsResult> response = restTemplate.exchange(url, HttpMethod.GET, request, TtsResult.class);
        TtsResult ttsResult = response.getBody();
        assert ttsResult != null;
        if (ttsResult.getCode() != 0) {
            throw new ServiceException(ttsResult.getMessage());
        }
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) ttsResult.getData();
        List<Map<String, Object>> onlineList = dataList.stream().filter(item -> item.get("presence").equals("online")).collect(Collectors.toList());

        return JSON.parseArray(JSON.toJSONString(onlineList));
    }


    @Override
    public JSONObject say(Session session, String text) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        String message = URLEncoder.DEFAULT.encode(jsonObject.toJSONString(), StandardCharsets.UTF_8);
        String url = StrUtil.format("https://api.mina.mi.com/remote/ubus?deviceId={}&message={}&method=text_to_speech&path=mibrain", session.getDeviceId(), message);

        HttpEntity<Object> request = new HttpEntity<>(null, getHeaders(session));
        TtsResult ttsResult = restTemplate.postForObject(URI.create(url), request, TtsResult.class);
        assert ttsResult != null;
        if (ttsResult.getCode() != 0) {
            throw new ServiceException(ttsResult.getMessage());
        }
        return JSON.parseObject(JSON.toJSONString(ttsResult.getData()));
    }

    private HttpHeaders getHeaders(Session session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HttpHeaders.COOKIE, session.getCookieList());
        return headers;
    }

    private String genClientSign(String nonce, String ssecurity) {
        String str = StrUtil.format("nonce={}&{}", nonce, ssecurity);
        String hashStr = Base64.encode(DigestUtil.sha1(str));
        return URLEncoder.DEFAULT.encode(hashStr, StandardCharsets.UTF_8);
    }
}
