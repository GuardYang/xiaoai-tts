package com.ruoyi.tts.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
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
        assert result != null;
        result = result.replace("&&&START&&&", "");
        return JSON.parseObject(result);
    }

    @Override
    public JSONObject loginMiAi(JSONObject authInfo) {
        String nonce = authInfo.getString("nonce");
        String ssecurity = authInfo.getString("ssecurity");
        String clientSign = genClientSign(nonce, ssecurity);

        String location = authInfo.getString("location");
        String url = location + "&clientSign=" + clientSign;

        ResponseEntity<String> response = restTemplate.getForEntity(URI.create(url), String.class);
        HttpHeaders headers = response.getHeaders();
        List<String> cookieList = headers.get(HttpHeaders.SET_COOKIE);
        JSONObject session = new JSONObject();
        session.put("cookieList", cookieList);
//        for (String cookie : cookieList) {
//            String key = cookie.split("=")[0];
//            session.put(key, cookie.substring(key.length() + 1).split(";")[0]);
//        }
        return session;
    }

    @Override
    public JSONArray getDevice(JSONObject session) {
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
    public JSONObject say(JSONObject session, String text) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        String message = URLEncoder.DEFAULT.encode(jsonObject.toJSONString(), StandardCharsets.UTF_8);
        String url = StrUtil.format("https://api.mina.mi.com/remote/ubus?deviceId=32238ec9-8c9d-438b-b073-f74f39b3a0cf&message={}&method=text_to_speech&path=mibrain", message);

        HttpEntity<Object> request = new HttpEntity<>(null, getHeaders(session));
        TtsResult ttsResult = restTemplate.postForObject(URI.create(url), request, TtsResult.class);
        assert ttsResult != null;
        if (ttsResult.getCode() != 0) {
            throw new ServiceException(ttsResult.getMessage());
        }
        return JSON.parseObject(JSON.toJSONString(ttsResult.getData()));
    }

    private HttpHeaders getHeaders(JSONObject session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        List<String> cookieList = session.getList("cookieList", String.class);
        headers.put(HttpHeaders.COOKIE, cookieList);
        return headers;
    }

    private String genClientSign(String nonce, String ssecurity) {
        String str = StrUtil.format("nonce={}&{}", nonce, ssecurity);
        String hashStr = Base64.encode(DigestUtil.sha1(str));
        return URLEncoder.DEFAULT.encode(hashStr, StandardCharsets.UTF_8);
    }
}
