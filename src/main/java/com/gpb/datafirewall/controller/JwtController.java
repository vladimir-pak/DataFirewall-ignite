package com.gpb.datafirewall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gpb.datafirewall.cef.SvoiApiLog;
import com.gpb.datafirewall.jwt.JwtUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class JwtController {
    private final JwtUtil jwtUtil;

    @Tag(name = "Get token", description = "Controller for getting token by secret")
    @GetMapping("{secret}")
    @SvoiApiLog(functionName = "Generating JWT")
    public String generateToken(@PathVariable String secret) {
        return jwtUtil.generateToken(secret);
    }
}
