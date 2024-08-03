package com.example.back_end.services;

import com.example.back_end.components.JwtTokenUtil;
import com.example.back_end.dtos.UserDTO;
import com.example.back_end.exceptions.DataNotFoundException;
import com.example.back_end.models.Role;
import com.example.back_end.models.User;
import com.example.back_end.repository.RoleReposiroty;
import com.example.back_end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final RoleReposiroty roleReposiroty;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    private final AuthenticationManager authenticationManager;
    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException {
        String email = userDTO.getEmail();
        if(userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .address(userDTO.getAddress())
                .build();

        Long customerRoleID = 2L;

//        Role existingRole = roleReposiroty.findById(userDTO.getRoleID())
//                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        Role existingRole = roleReposiroty.findById(customerRoleID)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        newUser.setRole(existingRole);

        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findUserByEmail(String email) throws DataNotFoundException {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public String login(String email, String password) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException("Email or password is invalid");
        }

        User existingUser = optionalUser.get();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password, existingUser.getAuthorities());

        // authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }


}
