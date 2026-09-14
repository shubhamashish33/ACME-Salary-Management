package com.acme.salary.auth;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    public record CurrentUser(String username, String role) {}
    @GetMapping("/me") CurrentUser me(Principal principal) { return new CurrentUser(principal.getName(), "HR_MANAGER"); }
}

