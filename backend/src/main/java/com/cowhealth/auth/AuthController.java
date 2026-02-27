package com.cowhealth.auth;

import com.cowhealth.auth.dto.LoginRequest;
import com.cowhealth.auth.dto.LoginResponse;
import com.cowhealth.common.ApiResponse;
import com.cowhealth.common.BusinessException;
import com.cowhealth.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!"admin".equals(request.getUsername()) || !"admin".equals(request.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }
        String token = tokenService.issueToken(request.getUsername());
        return ApiResponse.ok(new LoginResponse(token, "Bearer", TokenService.EXPIRE_SECONDS));
    }
}
