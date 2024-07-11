package com.example.back_end.services;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Product;
import com.example.back_end.repository.CrawlRepository;
import com.microsoft.playwright.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CrawlingService {
    @Value("${user.agent}")
    private String userAgent;
    @Autowired
    private Playwright playwright;
    private final WebDriver webDriver;

    @Autowired
    public CrawlingService(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public List<Product> crawlProduct(String url, List<String> keywords) {

        // Playwright
        Queue<String> listHref = new LinkedBlockingQueue<>();
        List<Product> productLinks = new ArrayList<>();

        try {
            // Playwright
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(userAgent)
            );

            Page page = context.newPage();
            // Điều hướng tới url
            page.navigate(url);

            // Bước 1: Thu thập các <a> chứa href theo tên từ khóa và đưa vào queue

            for(String keyword : keywords) {
                List<Locator> locators = page.locator("a[href*='" + keyword + "']").all();

                for(Locator locator: locators) {
                    ElementHandle element = locator.elementHandle();
                    String href = element.getAttribute("href");
                    listHref.add(href);
                }
            }


            // Bước 2: Truy cập vào các <a> trong queue để lấy sản phẩm và lấy danh sách <a> của sản phẩm
            while (!listHref.isEmpty()) {
                String link = listHref.poll();
                page.navigate(link);


                // Lấy thông tin sản phẩm
                String name = page.locator("h1").textContent();
                String priceString = page.locator(".price").textContent();
                Double price = Double.parseDouble(priceString);

                String discountString = page.locator(".discount").textContent();
                Double discount = Double.parseDouble(discountString);

                String saleString = page.locator(".sale").textContent();
                Double sale = Double.parseDouble(saleString);

                // Set thông tin cho product
                Product product = new Product();
                product.setName(name);
                product.setPrice(price);
                product.setDiscount(discount);
                product.setSalePrice(sale);

                productLinks.add(product);
            }

            page.close();
            context.close();
            browser.close();

            return productLinks;
        } catch (Exception e){
            // Xử lý ngoại lệ
            e.printStackTrace();
            return null;
        }
    }
}
