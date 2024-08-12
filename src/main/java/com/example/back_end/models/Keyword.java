package com.example.back_end.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keywordID")
    private Long id;

    @Column(name = "keyword_sale_url")
    private String keyword_sale_url;

    @Column(name = "keyword_wrapper")
    private String keyword_wrapper;

    @Column(name = "keyword_uptime")
    private String keyword_uptime;

    @Column(name = "keyword_title")
    private String keyword_title;

    @Column(name = "keyword_image")
    private String keyword_image;

    @Column(name = "keyword_price")
    private String keyword_price;

    @Column(name = "keyword_discount")
    private String keyword_discount;

    @Column(name = "keyword_sale")
    private String keyword_sale;

    @Column(name = "keyword_product")
    private String keyword_product;

    @Column(name = "keyword_review")
    private String keyword_review;

    @Column(name = "keyword_sold")
    private String keyword_sold;

    @OneToOne
    @JoinColumn(name = "crawlID")
    private Crawl crawl;
}
