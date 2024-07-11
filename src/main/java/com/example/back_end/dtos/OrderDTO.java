package com.example.back_end.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDTO {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("phone_number")
    @Size(max = 10, message = "Phone contains 10 number")
    @Min(value = 0, message = "Phone is not negative")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 200, message = "Address must be less than 200 characters")
    private String shippingAddress;

    @Min(value = 0, message = "Sale price is not negative")
    private Float totalPrice;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @Max(value = 200, message = "Note must be less than 200 characters")
    private String note;

    private String status;
}
