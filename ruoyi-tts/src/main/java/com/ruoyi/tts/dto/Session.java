package com.ruoyi.tts.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Session {
    private List<String> cookieList;
    private String deviceId;
}
