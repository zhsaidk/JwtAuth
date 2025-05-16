package com.zhsaidk.http.rest;

import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.TokenRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.LoginRequest;
import com.zhsaidk.dto.RefreshRequest;
import com.zhsaidk.dto.ResponseRequest;
import com.zhsaidk.jwt.JwtUtils;
import com.zhsaidk.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginRestController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody LoginRequest loginRequest,
                                                  BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HashMap<String, String> errors = new HashMap<>();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String accessToken = jwtUtils.generateAccessToken(authenticate);
        String refreshToken = jwtUtils.generateRefreshToken(authenticate);
        User user = userRepository.findUserByEmail(authenticate.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tokenRepository.save(
                Token.builder()
                        .user(user)
                        .refreshToken(refreshToken)
                        .build()
        );
        return ResponseEntity.ok(new ResponseRequest(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request){
        String refreshToken = request.getRefresh_token();
        if (!StringUtils.hasText(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }
        Optional<Token> rToken = tokenRepository.findByRefreshToken(refreshToken);
        if (rToken.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        if (!jwtUtils.validateJwtToken(refreshToken)){
            tokenRepository.delete(rToken.get());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired or invalid refresh token");
        }

        String username = jwtUtils.getUsernameFromJwtToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String generateAccessToken = jwtUtils.generateAccessToken(authenticationToken);
        String generateRefreshToken = jwtUtils.generateRefreshToken(authenticationToken);

        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        tokenRepository.delete(rToken.get());
        tokenRepository.save(
                Token.builder()
                        .user(user)
                        .refreshToken(generateRefreshToken)
                        .build());
        return ResponseEntity.ok(new ResponseRequest(generateAccessToken, generateRefreshToken));
    }
}
