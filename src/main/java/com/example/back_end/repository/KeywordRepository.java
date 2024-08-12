package com.example.back_end.repository;

import com.example.back_end.models.Keyword;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query("SELECT k FROM Keyword k WHERE k.crawl.id = :crawlId")
    Keyword findCrawlById(@Param("crawlId") Long crawlId);
}
