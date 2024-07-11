package com.example.back_end.controllers;

import com.example.back_end.models.User;
import com.example.back_end.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private AdminRepository adminRepository;

    @Autowired
    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @QueryMapping
    public Iterable<User> findAllUser() {
        return this.adminRepository.findAll();
    }

    @QueryMapping
    public User findUserById(@Argument Long id) {
        return this.adminRepository.findById(id).orElseThrow();
    }

    @QueryMapping
    public User findUserByEmail(@Argument String email) {
        return this.adminRepository.findUserByEmail(email).orElseThrow();
    }

}
