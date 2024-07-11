package com.example.back_end.controllers;

import com.example.back_end.dtos.CrawlDTO;
import com.example.back_end.dtos.RoleDTO;
import com.example.back_end.models.Crawl;
import com.example.back_end.models.Role;
import com.example.back_end.repository.CrawlRepository;
import com.example.back_end.services.CrawlingService;
import lombok.RequiredArgsConstructor;
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

    @QueryMapping
    public Iterable<Crawl> findAllCrawl() {
        return this.crawlRepository.findAll();
    }

    @MutationMapping
    public Crawl addCrawl(@Validated @Argument(name = "input") CrawlDTO crawlDTO) {
        Crawl crawl = new Crawl();
        crawl.setWebUrl(crawlDTO.getName());
        crawl.setStatus(crawlDTO.getStatus());
        return this.crawlRepository.save(crawl);
    }
}
