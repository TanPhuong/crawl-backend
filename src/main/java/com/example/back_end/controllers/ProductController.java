package com.example.back_end.controllers;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Product;
import com.example.back_end.models.Time;
import com.example.back_end.repository.ProductRepository;
import com.example.back_end.repository.TimeRepository;
import com.example.back_end.services.CrawlingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private ProductRepository productRepository;
    private TimeRepository timeRepository;
    private final CrawlingService crawlingService;

    @Autowired
    public ProductController(ProductRepository productRepository, CrawlingService crawlingService, TimeRepository timeRepository) {
        this.productRepository = productRepository;
        this.crawlingService = crawlingService;
        this.timeRepository = timeRepository;
    }

    @QueryMapping
    public Iterable<Product> findAllProduct() {



        LocalTime now = LocalTime.now();

        // Find time to crawl
        LocalTime nextTimeCrawl = null;
        if(this.timeRepository.count() > 0) {
            Iterable<Time> timeIterable = this.timeRepository.findAll();

            for(Time time: timeIterable) {
                LocalTime timeItem = time.getTimeCrawl();
                Duration shortestFutureDuration = null;

                if(timeItem.isAfter(now)) {
                    Duration duration = Duration.between(now, timeItem);
                    if (shortestFutureDuration == null || duration.compareTo(shortestFutureDuration) < 0) {
                        nextTimeCrawl = timeItem;
                        shortestFutureDuration = duration;
                    }
                }
            }
        }

        // Find url
        Iterable<Crawl> crawlIterable = this.crawlingService.findAll();
        List<Crawl> urlList = new ArrayList<>();
        for (Crawl crawl : crawlIterable) {
            urlList.add(crawl);
        }

        // Add Keyword
        List<String> keywords = new ArrayList<>();
        keywords.add("deal-hot");

        if(nextTimeCrawl != null) {
            if(now.isAfter(nextTimeCrawl)) {
                // Delete product and time crawl in database in order to crawl the new one
//                this.productRepository.deleteAll();
                this.timeRepository.deleteAll();

                // Crawling product
                for(Crawl url: urlList) {
                    String urlLink = url.getNameUrl();
                    Long urlID = url.getId();
                    if(url.getStatus()) {
                        List<Product> productList = this.crawlingService.crawlProduct(urlLink, keywords);
                        if(productList == null) {
                            break;
                        }

                        for(Product product: productList) {
                            this.productRepository.save(product);
                        }
                    }
                }
            }
        } else if(this.productRepository.count() < 30) {

            this.timeRepository.deleteAll();
            // Crawling product
            for(Crawl url: urlList) {
                String urlLink = url.getNameUrl();
                Long urlID = url.getId();
                if(url.getStatus()) {
                    List<Product> productList = this.crawlingService.crawlProduct(urlLink, keywords);
                    if(productList == null) {
                        break;
                    }

                    for(Product product: productList) {
                        this.productRepository.save(product);
                    }
                }
            }
        }

        System.out.println(now);
        return this.productRepository.findAll();
    }

    @QueryMapping
    public Iterable<Product> prioritizeProduct() {

        Iterable<Product> productsList = this.productRepository.prioritizeProduct();

        return productsList;
    }
}
