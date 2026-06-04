package com.gpb.datafirewall.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gpb.datafirewall.cef.SvoiApiLog;
import com.gpb.datafirewall.service.ToggleHandlerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HandlerController {
    private final ToggleHandlerService toggleHandlerService;

    /**
     * Метод для переключения обработчика сообщений MQ
     */
    @SvoiApiLog(functionName = "Toggle handler")
    @PostMapping("/handler")
    public void toggleHandler(@RequestBody Map<String, String> body) {
        String handler = body.get("handler");
        if (handler == null) {
            throw new IllegalArgumentException("Missing required parameter: handler");
        }
        toggleHandlerService.toggle(handler);
    }
}
