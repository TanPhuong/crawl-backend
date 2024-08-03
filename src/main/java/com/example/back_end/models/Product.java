package com.example.back_end.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productID")
    private Long id;

    @Column(name = "name_product")
    private String name;

    @Column(name = "price")
    private Float price;

    @Column(name = "discount")
    private Float discount;

    @Column(name = "sale_price")
    private Float salePrice;

    @Column(name = "url")
    private String url;

    @Column(name = "image")
    private String image;

    @Column(name = "review")
    private Float review;

    @Column(name = "sold")
    private Float sold;

    @ManyToOne
    @JoinColumn(name = "crawlID")
    private Crawl crawl;
}
