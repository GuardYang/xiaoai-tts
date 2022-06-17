package com.ruoyi.tts.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/tts")
public class TestTtsController {
    @RequestMapping("/hello")
    public AjaxResult hello() {
        return AjaxResult.success();
    }
}
