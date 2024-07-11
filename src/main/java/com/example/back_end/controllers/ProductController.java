package com.example.back_end.controllers;

import com.example.back_end.models.Product;
import com.example.back_end.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private ProductRepository productRepository;

    @QueryMapping
    public Iterable<Product> findAllProduct() {
        return this.productRepository.findAll();
    }

    
}
