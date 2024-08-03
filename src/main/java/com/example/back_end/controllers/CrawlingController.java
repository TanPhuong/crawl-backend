package com.example.back_end.controllers;

import com.example.back_end.dtos.CrawlDTO;
import com.example.back_end.dtos.RoleDTO;
import com.example.back_end.models.Crawl;
import com.example.back_end.models.Role;
import com.example.back_end.repository.CrawlRepository;
import com.example.back_end.services.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
public class CrawlingController {
    private final CrawlingService crawlingService;
    private CrawlRepository crawlRepository;

    @Autowired
    public CrawlingController(CrawlRepository crawlRepository, CrawlingService crawlingService) {
        this.crawlRepository = crawlRepository;
        this.crawlingService = crawlingService;
    }

    @QueryMapping
    public Iterable<Crawl> findAllCrawl() {
        return this.crawlingService.findAll();
    }

    @MutationMapping
    public Crawl addCrawl(@Validated @Argument(name = "input") CrawlDTO crawlDTO) {
        Crawl crawl = new Crawl();
        crawl.setNameUrl(crawlDTO.getNameUrl());
        crawl.setStatus(crawlDTO.getStatus());
        return this.crawlRepository.save(crawl);
    }
}
