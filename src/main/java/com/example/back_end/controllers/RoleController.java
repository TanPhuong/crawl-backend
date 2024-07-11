package com.example.back_end.controllers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.example.back_end.dtos.RoleDTO;
import com.example.back_end.models.Role;
import com.example.back_end.repository.RoleReposiroty;

import com.example.back_end.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class RoleController {
    private RoleReposiroty roleReposiroty;
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleReposiroty roleReposiroty, RoleService roleService) {
        this.roleReposiroty = roleReposiroty;
        this.roleService = roleService;
    }

    @QueryMapping
    public Iterable<Role> findAllRole() {
        return this.roleReposiroty.findAll();
    }

    @QueryMapping
    public Role findRoleById(@Argument Long id) {
        return this.roleReposiroty.findById(id).orElseThrow();
    }


    @MutationMapping
    public Role addRole(@Validated @Argument(name = "input") RoleDTO roleDTO) {
        Role role = new Role();
        role.setName(roleDTO.getName());
        return this.roleReposiroty.save(role);
    }

    @MutationMapping
    public Role updateRole(@Argument Long id, @Argument RoleDTO roleInput) {
        return this.roleService.updateRole(id, roleInput);
    }

    @MutationMapping
    public String deleteRole(@Argument Long id) {
        return this.roleService.deleteRole(id);
    }

}
