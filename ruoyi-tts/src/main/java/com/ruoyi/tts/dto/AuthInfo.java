package com.ruoyi.tts.dto;

import lombok.Data;

@Data
public class AuthInfo {
    private Integer code;
    private String description;
    private String nonce;
    private String ssecurity;
    private String location;
}