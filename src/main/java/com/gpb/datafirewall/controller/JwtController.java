package com.gpb.datafirewall.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gpb.datafirewall.cef.SvoiApiLog;
import com.gpb.datafirewall.dto.RequestJwtDto;
import com.gpb.datafirewall.jwt.JwtUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class JwtController {
    private final JwtUtil jwtUtil;

    @Tag(name = "Get token", description = "Controller for getting token by secret + service")
    @PostMapping("create")
    @SvoiApiLog(functionName = "Generating JWT")
    public String generateToken(@RequestBody RequestJwtDto body) {
        return jwtUtil.generateToken(
            body.getSecret(),
            body.getService()
        );
    }

    @Tag(name = "Revoke token", description = "Controller for revoking token by service")
    @DeleteMapping("revoke")
    @SvoiApiLog(functionName = "Generating JWT")
    public String revokeToken(@RequestBody RequestJwtDto body) {
        return jwtUtil.generateToken(
            body.getSecret(),
            body.getService()
        );
    }
}
