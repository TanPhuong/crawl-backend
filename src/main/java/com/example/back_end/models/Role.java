package com.example.back_end.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleID")
    private Long id;

    @Column(name = "name", length = 20)
    private String name;
}
