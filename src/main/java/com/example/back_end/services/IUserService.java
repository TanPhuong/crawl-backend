package com.example.back_end.services;

import com.example.back_end.dtos.UserDTO;
import com.example.back_end.exceptions.DataNotFoundException;
import com.example.back_end.models.User;

import java.util.Optional;

public interface IUserService {
    User createUser(UserDTO userDTO) throws DataNotFoundException;

    Optional<User> findUserByEmail(String email) throws  DataNotFoundException;

    String login(String email, String password) throws Exception;
}
