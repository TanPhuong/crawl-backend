package com.example.back_end.services;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Product;
import com.example.back_end.repository.CrawlRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.Page.WaitForNavigationOptions;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class CrawlingService {
    @Value("${user.agent}")
    private String userAgent;
    @Autowired
    private Playwright playwright;

    private CrawlRepository crawlRepository;

    @Autowired
    public CrawlingService(Playwright playwright, CrawlRepository crawlRepository) {
        this.crawlRepository = crawlRepository;
        this.playwright = playwright;
    }

    public Iterable<Crawl> findAll() {
        return this.crawlRepository.findAll();
    }

    public List<Product> crawlProduct(String url, List<String> keywords) {

        // Playwright
        Queue<String> listHref = new LinkedBlockingQueue<>();
        List<Product> productList = new ArrayList<>();

        try {
            // Playwright
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setProxy(new Proxy("http://45.127.248.127:5128")
                            .setUsername("nlurysba")
                            .setPassword("3r9nz50smr31")));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(userAgent)
            );

            Page page = context.newPage();
            // Điều hướng tới url
            page.navigate(url);
            page.waitForLoadState(LoadState.LOAD);

            // Bước 1: Thu thập các <a> chứa href theo tên từ khóa và đưa vào queue

            for(String keyword : keywords) {
                List<Locator> locators = page.locator("a[href*='" + keyword + "']").all();

                for(Locator locator: locators) {
                    ElementHandle element = locator.elementHandle();
                    String href = element.getAttribute("href");
                    if(href.contains("?")) {
                        listHref.add(href);
                    }
                }
            }


            // Bước 2: Truy cập vào các <a> trong queue để lấy sản phẩm và lấy danh sách <a> của sản phẩm
            while (!listHref.isEmpty()) {
                String link = listHref.poll();

                // Skip product from main page
                if(link.contains("from_item")) {
                    continue;
                }

                // Get link to the detail of flash sale page to get product list
                if(!link.contains(url)) {
                    String combineLink = url + link;
                    System.out.println(combineLink);
                    page.navigate(combineLink);
                } else {
                    page.navigate(link);
                    System.out.println(link);
                }

                page.waitForLoadState(LoadState.LOAD);


                // Get product information
                // 1. Get Title product
                List<String> productTitles = page.locator("div[class*='ProductTitle']")
                        .all()
                        .stream()
                        .map(element -> element.textContent())
                        .collect(Collectors.toList());

                // 2. Get original price (before sale)
                List<String> originalPriceString = page.locator("div[class*='OriginalPrice']")
                        .all()
                        .stream()
                        .map(element -> element.textContent())
                        .collect(Collectors.toList());

                List<Double> originalPrices = originalPriceString.stream()
                        .map(priceStr -> {
                            String numericPart = priceStr.replaceAll("[^0-9.-]+", "");
                            if (numericPart.contains("-") || numericPart.contains(".")) {
                                // Loại bỏ tất cả các dấu trừ và dấu chấm trong numericPart
                                numericPart = numericPart.replaceAll("[-.]", "");
                            }
                            return Double.parseDouble(numericPart);
                        })
                        .collect(Collectors.toList());

                // 3. Get sale percentage
                List<String> discountPercentageString = page.locator("div[class*='DiscountPercentage']")
                        .all()
                        .stream()
                        .map(element -> element.textContent())
                        .collect(Collectors.toList());

                List<Double> discountPercentages = discountPercentageString.stream()
                        .map(priceStr -> {
                            String numericPart = priceStr.replaceAll("[^0-9.-]+", "");
                            if (numericPart.contains("-") || numericPart.contains(".")) {
                                // Loại bỏ tất cả các dấu trừ và dấu chấm trong numericPart
                                numericPart = numericPart.replaceAll("[-.]", "");
                            }
                            return Double.parseDouble(numericPart);
                        })
                        .collect(Collectors.toList());


                // 4. Get discount price (after sale)
                List<String> discountPriceString = page.locator("div[class*='DiscountedPrice']")
                        .all()
                        .stream()
                        .map(element -> element.textContent())
                        .collect(Collectors.toList());

                List<Double> discountPrices = discountPriceString.stream()
                        .map(priceStr -> {
                            String numericPart = priceStr.replaceAll("[^0-9.-]+", "");
                            if (numericPart.contains("-") || numericPart.contains(".")) {
                                // Loại bỏ tất cả các dấu trừ và dấu chấm trong numericPart
                                numericPart = numericPart.replaceAll("[-.]", "");
                            }
                            return Double.parseDouble(numericPart);
                        })
                        .collect(Collectors.toList());

                // 5. Get URL of products
                List<String> productLinks = page.locator("a[data-view-id*='flashdeal']")
                        .all()
                        .stream()
                        .map(element -> element.getAttribute("href"))
                        .filter(href -> href != null && href.contains("https"))
                        .collect(Collectors.toList());

                List<String> shortenedProductLinks = productLinks.stream()
                        .map(shortenedLink -> {
                            int questionIndex = shortenedLink.indexOf('?');
                            if (questionIndex != -1) {
                                return shortenedLink.substring(0, questionIndex + 1);
                            } else {
                                return shortenedLink;
                            }
                        })
                        .collect(Collectors.toList());

                System.out.println(productTitles.size());
                for(String title : productTitles) {
                    System.out.println(title);
                }

                System.out.println(originalPrices.size());
                for(Double price: originalPrices) {
                    System.out.println(price);
                }

                System.out.println(discountPrices.size());
                for(Double price: discountPrices) {
                    System.out.println(price);
                }

                System.out.println(discountPercentages.size());
                for(Double price: discountPercentages) {
                    System.out.println(price);
                }

                System.out.println(productLinks.size());
                for(String links: productLinks) {
                    System.out.println(links);
                }

                // Set information for product
//                for(int i = 0; i < productTitles.size(); i++) {
//                    Product product = new Product();
//                    product.setName(productTitles.get(i));
//                    product.setPrice(originalPrices.get(i));
//                    product.setDiscount(discountPercentages.get(i));
//                    product.setSalePrice(discountPrices.get(i));
//                    product.setUrl(shortenedProductLinks.get(i));
//
//                    productList.add(product);
//                }
            }

            page.close();
            context.close();
            browser.close();

            return productList;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
