package com.example.back_end.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crawl")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class Crawl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawlID")
    private Long id;

    @Column(name = "name_url")
    private String nameUrl;

    @Column(name = "status")
    private Boolean status;
}
