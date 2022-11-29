package spring.application.tree.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.application.tree.data.exceptions.SecurityException;
import spring.application.tree.data.users.security.token.AuthenticationProcessingService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationProcessingService authenticationProcessingService;

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestParam("username") String username,
                                            @RequestParam("password") String password,
                                            HttpServletRequest request) throws SecurityException {
        return ResponseEntity.ok(authenticationProcessingService.authenticateUserWithTokenBasedAuthorizationStrategy(username, password, request));
    }
}
