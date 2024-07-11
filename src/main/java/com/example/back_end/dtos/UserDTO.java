package com.example.back_end.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("phone_number")
    @Min(value = 0, message = "Phone is not negative")
    private Long phoneNumber;

    @NotBlank(message = "Email must be written")
    private String email;

    @Size(min = 5, max = 200, message = "Address must be less than 200 characters")
    private String address;

    @NotNull(message = "Role ID is required")
    @JsonProperty("roleID")
    private Long roleID;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @JsonProperty("retype_password")
    private String retypePassword;

}
