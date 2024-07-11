package com.example.back_end.controllers;

import com.example.back_end.dtos.UserLoginDTO;
import com.example.back_end.models.Role;
import com.example.back_end.models.User;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.example.back_end.dtos.*;
import com.example.back_end.responses.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body("Password does not match");
            }

            User newUser = userService.createUser(userDTO);

            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<loginResponse> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = userService.login(userLoginDTO.getEmail(), userLoginDTO.getPassword());
            Optional<User> existUser = userService.findUserByEmail(userLoginDTO.getEmail());
            return ResponseEntity.ok(loginResponse.builder()
                            .user(existUser)
                            .token(token)
                    .build());
//            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(loginResponse.builder()
                            .user(null)
                            .token(e.getMessage())
                    .build());
//            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
