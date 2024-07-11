package com.example.back_end.responses;

import com.example.back_end.models.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class loginResponse {

    private Optional<User> user;

    @JsonProperty("token")
    private String token;
}
