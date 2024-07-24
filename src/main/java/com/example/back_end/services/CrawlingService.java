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
import java.util.concurrent.TimeoutException;
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

    private boolean checkString(Queue<String> queue,String duplicate) {
        for(String checkString : queue) {
            if(checkString.contains(duplicate)) {
                return false;
            }
        }
        return true;
    }

    public Iterable<Crawl> findAll() {
        return this.crawlRepository.findAll();
    }

    public List<Product> crawlProduct(String url, List<String> keywords) {

        // Playwright
        Queue<String> listHref = new LinkedBlockingQueue<>();
        List<Product> productList = new ArrayList<>();

        Queue<String> productHref = new LinkedBlockingQueue<>();

        try {
            // Playwright
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions());
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(userAgent)
//                    .setProxy(new Proxy("http://45.127.248.127:5128")
//                            .setUsername("nlurysba")
//                            .setPassword("3r9nz50smr31"))
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
                    // Thêm code cho không trùng keyword như tiki
                    if(checkString(listHref, keyword)) {
                        listHref.add(href);
                    }
                }
            }


            // Bước 2: Truy cập vào các <a> trong queue để lấy sản phẩm và lấy danh sách <a> của sản phẩm

            // Crawl per Product in Wrapper/Container
            while (!listHref.isEmpty()) {
                String link = listHref.poll();


                // Skip product from main page
                if (link.contains("from_item")) {
                    continue;
                }

                // Get link to the detail of flash sale page to get product list
                if (!link.contains(url)) {
                    String combineLink = url + link;
                    System.out.println(combineLink);
                    page.navigate(combineLink);
                } else {
                    page.navigate(link);
                    System.out.println(link);
                }

                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForLoadState(LoadState.NETWORKIDLE);

                // Get product information

                // Get size of the product container
                List<Locator> productTitleList = page.locator("div[class*='ProductTitle']").all();


                for (int i = 0; i < productTitleList.size(); i++) {

                    // 1. Name Product
                    String productTitle = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='ProductTitle']").nth(i).textContent();

                    // 2. Original Price
                    String originalPriceString = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='OriginalPrice']").nth(i).textContent();
                    String convertOriginalPrice = originalPriceString.replaceAll("[^0-9.-]+", "");
                    Double originalPrice = Double.parseDouble(convertOriginalPrice.replace(".",""));
                    if(originalPriceString == "") {
                        originalPrice = 0.0;
                    }

                    // 3. Sale Percentage
                    String discountPercentageString = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='DiscountPercentage']").nth(i).textContent();
                    Double discountPercentage = Double.parseDouble(discountPercentageString.replaceAll("[^0-9.-]+", ""));
                    if(discountPercentageString == "") {
                        discountPercentage = 0.0;
                    }

                    // 4. Discount price
                    String discountedPriceString = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='DiscountedPrice']").nth(i).textContent();
                    String convertDiscountedPrice = discountedPriceString.replaceAll("[^0-9.-]+", "");
                    Double discountedPrice = Double.parseDouble(convertDiscountedPrice.replace(".",""));
                    if(discountedPriceString == "") {
                        discountedPrice = 0.0;
                    }

                    // 5. Url of Product
                    String urlLink = page.locator("div[class*='Wrapper']")
                            .locator("a[data-view-id*='flashdeal']").nth(i).textContent();

                    page.waitForTimeout(4000);

                    // Create new browser context to add proxy pool when navigate to each product url
                    BrowserContext contextPerPage = browser.newContext(new Browser.NewContextOptions()
                            .setUserAgent(userAgent)
                            .setProxy(new Proxy("http://45.127.248.127:5128")
                                    .setUsername("nlurysba")
                                    .setPassword("3r9nz50smr31"))
                    );

                    Page perPage = contextPerPage.newPage();

                    perPage.navigate(urlLink);
                    perPage.waitForLoadState(LoadState.LOAD);

                    // 6. Get review quantity
                    String reviewQuantityString = page.locator("a[data-view-id*='view_review']").textContent();
                    String convertReviewQuantity = reviewQuantityString.replaceAll("[^0-9.-]+", "");
                    Double reviewQuantity = Double.parseDouble(convertReviewQuantity);
                    if(reviewQuantityString == "") {
                        reviewQuantity = 0.0;
                    }

                    // 7. Get sold quantity
                    String soldQuantityString = page.locator("div[data-view-id*='quantity_sold']").textContent();
                    String convertSoldQuantity = soldQuantityString.replaceAll("[^0-9.-]+", "");
                    Double soldQuantity = Double.parseDouble(convertSoldQuantity);
                    if(soldQuantityString == "") {
                        soldQuantity = 0.0;
                    }

//                    System.out.println(productTitle);
//                    System.out.println(originalPrice);
//                    System.out.println(discountPercentage);
//                    System.out.println(discountedPrice);
//                    System.out.println(urlLink);
//                    System.out.println(reviewQuantity);
//                    System.out.println(soldQuantity);

//                    Product product = new Product();
//                    product.setName(productTitle);
//                    product.setPrice(originalPrice);
//                    product.setDiscount(discountPercentage);
//                    product.setSalePrice(discountedPrice);
//                    product.setUrl(urlLink);
//                    product.setReview(reviewQuantity);
//                    product.setSold(soldQuantity);
//
//                    productList.add(product);

                    perPage.close();

                }
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
