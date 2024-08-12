package com.example.back_end.services;

import com.example.back_end.dtos.RoleDTO;
import com.example.back_end.models.Role;
import com.example.back_end.repository.RoleReposiroty;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleReposiroty roleReposiroty;

    public RoleService(RoleReposiroty roleReposiroty) {
        this.roleReposiroty = roleReposiroty;
    }

    public Iterable<Role> findAllRole() {
        return this.roleReposiroty.findAll();
    }

    public Role updateRole(Long id, Role roleInput) {
        Role role = this.roleReposiroty.findById(id).orElseThrow(() -> new RuntimeException("ID is not exist"));
        role.setId(id);
        role.setName(roleInput.getName());
        return this.roleReposiroty.save(role);
    }

    public String deleteRole(Long id) {
        Role role = this.roleReposiroty.findById(id).orElseThrow(() -> new RuntimeException("ID is not exist"));
        this.roleReposiroty.delete(role);
        return "Role is deleted " + id;
    }
}
