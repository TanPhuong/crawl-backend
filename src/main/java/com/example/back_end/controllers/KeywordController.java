package com.example.back_end.controllers;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Keyword;
import com.example.back_end.repository.KeywordRepository;
import com.example.back_end.services.CrawlingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class KeywordController {
    private KeywordRepository keywordRepository;
    private CrawlingService crawlingService;

    public KeywordController(KeywordRepository keywordRepository, CrawlingService crawlingService) {
        this.keywordRepository = keywordRepository;
        this.crawlingService = crawlingService;
    }

    @QueryMapping
    public Iterable<Keyword> findAllKeyword() {
        return this.keywordRepository.findAll();
    }

    @QueryMapping
    public Keyword findKeywordByCrawl(@Argument(name = "input") Crawl crawl) {
        Keyword keyword = this.crawlingService.writeConfig(crawl);
        System.out.println(keyword);

//        this.keywordRepository.save(keyword);
        return this.keywordRepository.findById(keyword.getId()).orElseThrow();
    }

    @MutationMapping
    public Keyword addKeyword(@Argument(name = "input") Keyword keyword) {
        Keyword newKeyword = new Keyword();
        newKeyword.setId(keyword.getId());
        newKeyword.setKeyword_sale_url(keyword.getKeyword_sale_url());
        newKeyword.setKeyword_wrapper(keyword.getKeyword_wrapper());
        newKeyword.setKeyword_uptime(keyword.getKeyword_uptime());
        newKeyword.setKeyword_title(keyword.getKeyword_title());
        newKeyword.setKeyword_image(keyword.getKeyword_image());
        newKeyword.setKeyword_price(keyword.getKeyword_price());
        newKeyword.setKeyword_discount(keyword.getKeyword_discount());
        newKeyword.setKeyword_sale(keyword.getKeyword_sale());
        newKeyword.setKeyword_product(keyword.getKeyword_product());
        newKeyword.setKeyword_review(keyword.getKeyword_review());
        newKeyword.setKeyword_sold(keyword.getKeyword_sold());
        newKeyword.setCrawl(keyword.getCrawl());

        return newKeyword;
    }

    @MutationMapping
    public Keyword updateKeyword(@Argument(name = "id") Long id, @Argument(name = "input") Keyword keyword) {
        Keyword newKeyword = new Keyword();
        newKeyword.setId(id);
        newKeyword.setKeyword_sale_url(keyword.getKeyword_sale_url());
        newKeyword.setKeyword_wrapper(keyword.getKeyword_wrapper());
        newKeyword.setKeyword_uptime(keyword.getKeyword_uptime());
        newKeyword.setKeyword_title(keyword.getKeyword_title());
        newKeyword.setKeyword_image(keyword.getKeyword_image());
        newKeyword.setKeyword_price(keyword.getKeyword_price());
        newKeyword.setKeyword_discount(keyword.getKeyword_discount());
        newKeyword.setKeyword_sale(keyword.getKeyword_sale());
        newKeyword.setKeyword_product(keyword.getKeyword_product());
        newKeyword.setKeyword_review(keyword.getKeyword_review());
        newKeyword.setKeyword_sold(keyword.getKeyword_sold());
        newKeyword.setCrawl(keyword.getCrawl());

        return newKeyword;
    }
}
