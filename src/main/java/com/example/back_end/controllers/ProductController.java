package com.example.back_end.controllers;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Product;
import com.example.back_end.repository.ProductRepository;
import com.example.back_end.services.CrawlingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private ProductRepository productRepository;
    private final CrawlingService crawlingService;

    @Autowired
    public ProductController(ProductRepository productRepository, CrawlingService crawlingService) {
        this.productRepository = productRepository;
        this.crawlingService = crawlingService;
    }

    @QueryMapping
    public Iterable<Product> findAllProduct() {
        // Find url
        Iterable<Crawl> crawlIterable = this.crawlingService.findAll();
        List<Crawl> urlList = new ArrayList<>();
        for (Crawl crawl : crawlIterable) {
            urlList.add(crawl);
        }

        // Add Keyword
        List<String> keywords = new ArrayList<>();
        keywords.add("deal-hot");
//        keywords.add("flash");
//        keywords.add("flash-sale");

        // Crawling product
        for(Crawl url: urlList) {
            String urlLink = url.getNameUrl();
            if(url.getStatus()) {
                List<Product> productList = this.crawlingService.crawlProduct(urlLink, keywords);
                if(productList == null) {
                    break;
                }

                for(Product product: productList) {
                    this.productRepository.deleteAll();
                    this.productRepository.save(product);
                }
            }
        }
        return this.productRepository.findAll();
    }

}
