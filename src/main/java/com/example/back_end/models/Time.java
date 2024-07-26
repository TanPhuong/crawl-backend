package com.example.back_end.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeID")
    private Long id;

    @Column(name = "time_crawl")
    private LocalTime timeCrawl;

    @Column(name = "date_crawl")
    private LocalDate dateCrawl;

    @ManyToOne
    @JoinColumn(name = "crawlID")
    private Long crawlID;
}
