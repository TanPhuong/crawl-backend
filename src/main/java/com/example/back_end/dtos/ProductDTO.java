package com.example.back_end.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Price is required")
    private Float price;

    private Float salePrice;

    @NotBlank(message = "Discount is required")
    private Float discount;

    @NotBlank(message = "URL is required")
    private String url;

    private String image;

    private Float review;

    private Float sold;
}
