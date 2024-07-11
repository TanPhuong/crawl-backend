package com.example.back_end.repository;

import com.example.back_end.models.Crawl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlRepository extends JpaRepository<Crawl, Long> {
}
