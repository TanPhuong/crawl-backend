package com.example.back_end.dtos;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CrawlDTO {
    @Size(min = 3, max = 50, message = "Url must be between 3 and 50 characters")
    private String nameUrl;

    private Boolean status;
}
