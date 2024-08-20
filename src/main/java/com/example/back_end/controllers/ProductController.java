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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public Iterable<Product> getAllProduct() {
        return this.productRepository.findAll();
    }

    @QueryMapping
    public Iterable<Product> prioritizeProduct() {
        return this.productRepository.prioritizeProduct();
    }

    @QueryMapping
    public Iterable<Product> findProductByPriceDESC() {
        return this.productRepository.findProductByPriceDESC();
    }

    @QueryMapping
    public Iterable<Product> findProductByDiscount() {
        return this.productRepository.findProductByDiscount();
    }

    @QueryMapping
    public Iterable<Product> realTimeCrawl() {

        // Find url
        Iterable<Crawl> crawlIterable = this.crawlingService.findAll();
        List<Crawl> urlList = new ArrayList<>();
        for (Crawl crawl : crawlIterable) {
            urlList.add(crawl);
        }

        // Add Keyword
        List<String> keywords = new ArrayList<>();
        keywords.add("deal-hot");

        // Crawling product
        for(Crawl url: urlList) {
            if(url.getStatus()) {
                List<Product> productList = this.crawlingService.realTimeCrawling(url, keywords);
                if(productList == null) {
                    break;
                }
            }
        }

        return this.productRepository.findAll();
    }

    @QueryMapping
    public Iterable<Product> findAllProduct() {

        LocalTime now = LocalTime.now();
        LocalDate nowDate = LocalDate.now();

        // Find time to crawl
        LocalTime nextTimeCrawl = null;
        LocalDate dateCrawl = null;
        if(this.timeRepository.count() > 0) {
            Iterable<Time> timeIterable = this.timeRepository.findAll();

            for(Time time: timeIterable) {
                LocalTime timeItem = time.getTimeCrawl();
                dateCrawl = time.getDateCrawl();

                if(now.isAfter(timeItem)) {
                    nextTimeCrawl = timeItem;
                    break;
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

        if(this.productRepository.count() < 30) {
            this.timeRepository.deleteAll();
            this.productRepository.deleteAll();
            // Crawling product
            for(Crawl url: urlList) {
                String urlLink = url.getNameUrl();
                if(url.getStatus()) {
                    List<Product> productList = this.crawlingService.crawlProduct(urlLink, keywords, url);
                    if(productList == null) {
                        break;
                    }
                }
            }
        } else if(nextTimeCrawl != null) {
            if(now.isAfter(nextTimeCrawl) || nowDate.isAfter(dateCrawl)) {
                this.timeRepository.deleteAll();
                this.productRepository.deleteAll();
                // Crawling product
                for(Crawl url: urlList) {
                    String urlLink = url.getNameUrl();
                    if(url.getStatus()) {
                        List<Product> productList = this.crawlingService.crawlProduct(urlLink, keywords, url);
                        if(productList == null) {
                            break;
                        }
                    }
                }
            }
        }

        System.out.println(nextTimeCrawl);
        return this.productRepository.findAll();
    }

}
