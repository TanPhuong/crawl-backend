package com.example.back_end.repository;

import com.example.back_end.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p ORDER BY p.sold DESC, p.review DESC")
    Iterable<Product> prioritizeProduct();
}
