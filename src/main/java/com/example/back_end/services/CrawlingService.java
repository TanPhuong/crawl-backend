package com.example.back_end.services;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Product;
import com.example.back_end.models.Time;
import com.example.back_end.repository.CrawlRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.Page.WaitForNavigationOptions;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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

                // Get time flash sale
                try {
                    List<Locator> saleTimeList = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='upcoming-time']").all();

                    if(saleTimeList.isEmpty()) {
                        throw new Exception("No elements found with the specified selector.");
                    }

                    for (Locator saleTimeLocator : saleTimeList) {
                        ElementHandle element = saleTimeLocator.elementHandle();
                        String saleTimeString = element.textContent();
                        LocalTime saleTime = LocalTime.parse(saleTimeString);

                        System.out.println(saleTime);

                        // create crawl entity to save to time in database
                        Crawl crawl = new Crawl();
                        crawl.setNameUrl(url);
                        crawl.setStatus(true);

                        Time time = new Time();
                        time.setTimeCrawl(saleTime);
                        time.setDateCrawl(LocalDate.now());
                        time.setCrawl(crawl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Get product information

                // Get size of the product container
                List<Locator> productTitleList = page.locator("div[class*='ProductTitle']").all();


                for (int i = 0; i < productTitleList.size(); i++) {

                    // 1. Name Product
                    String productTitle = page.locator("div[class*='Wrapper']")
                            .locator("div[class*='ProductTitle']").nth(i).textContent();

                    // 2. Product Image
                    String imageProduct = page.locator("div[class*='Wrapper']")
                            .locator("img[class*='WebpImg']").nth(i).getAttribute("src");

                    // 3. Original Price
                    Float originalPrice;
                    try {
                        Locator originalPriceLocator = page.locator("div[class*='Wrapper']")
                                .locator("div[class*='OriginalPrice']").nth(i);
                        originalPriceLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String originalPriceString = originalPriceLocator.elementHandle().textContent();

//                        String originalPriceString = page.locator("div[class*='Wrapper']")
//                                .locator("div[class*='OriginalPrice']").nth(i).textContent();

                        String convertOriginalPrice = originalPriceString.replaceAll("[^0-9.-]+", "");
                        originalPrice = Float.parseFloat(convertOriginalPrice.replace(".",""));
                    } catch (Exception e) {
                        originalPrice = 0f;
                    }

                    // 4. Sale Percentage
                    Float discountPercentage;
                    try {

                        Locator discountPercentageLocator = page.locator("div[class*='Wrapper']")
                                .locator("div[class*='DiscountPercentage']").nth(i);
                        discountPercentageLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String discountPercentageString = discountPercentageLocator.elementHandle().textContent();

//                        String discountPercentageString = page.locator("div[class*='Wrapper']")
//                                .locator("div[class*='DiscountPercentage']").nth(i).textContent();

                        discountPercentage = Float.parseFloat(discountPercentageString.replaceAll("[^0-9.-]+", ""));
                    } catch (Exception e) {
                        discountPercentage = 0f;
                    }

                    // 5. Discount price
                    Float discountedPrice;
                    try {
                        Locator discountedPriceLocator = page.locator("div[class*='Wrapper']")
                                .locator("div[class*='DiscountedPrice']").nth(i);
                        discountedPriceLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String discountPercentageString = discountedPriceLocator.elementHandle().textContent();

//                        String discountedPriceString = page.locator("div[class*='Wrapper']")
//                                .locator("div[class*='DiscountedPrice']").nth(i).textContent();

                        String convertDiscountedPrice = discountPercentageString.replaceAll("[^0-9.-]+", "");
                        discountedPrice = Float.parseFloat(convertDiscountedPrice.replace(".",""));
                    } catch (Exception e) {
                        discountedPrice = 0f;
                    }

                    // 6. Url of Product
                    String urlLink = page.locator("div[class*='Wrapper']")
                            .locator("a[data-view-id*='flashdeal']").nth(i).getAttribute("href");

                    page.waitForTimeout(4000);

//                     Create new browser context to add proxy pool when navigate to each product url
                    BrowserContext contextPerPage = browser.newContext(new Browser.NewContextOptions()
                            .setUserAgent(userAgent)
//                            .setProxy(new Proxy("http://45.127.248.127:5128")
//                                    .setUsername("nlurysba")
//                                    .setPassword("3r9nz50smr31"))
                    );

                    Page perPage = contextPerPage.newPage();

                    perPage.navigate(urlLink);
                    perPage.waitForLoadState(LoadState.NETWORKIDLE);

                    // 7. Get review quantity

                    Float reviewQuantity;
                    try {
                        perPage.waitForSelector("a[data-view-id*='view_review']", new Page.WaitForSelectorOptions().setTimeout(3000));

                        String reviewQuantityString = perPage.locator("a[data-view-id*='view_review']").textContent();
                        String convertReviewQuantity = reviewQuantityString.replaceAll("[^0-9.-]+", "");
                        reviewQuantity = Float.parseFloat(convertReviewQuantity);
                    } catch (Exception e) {
                        reviewQuantity = 0f;
                    }

                    // 8. Get sold quantity
                    Float soldQuantity;
                    try {
                        perPage.waitForSelector("div[data-view-id*='quantity_sold']", new Page.WaitForSelectorOptions().setTimeout(3000));

                        String soldQuantityString = perPage.locator("div[data-view-id*='quantity_sold']").textContent();
                        String convertSoldQuantity = soldQuantityString.replaceAll("[^0-9.-]+", "");
                        soldQuantity = Float.parseFloat(convertSoldQuantity);
                    } catch (Exception e) {
                        soldQuantity = 0f;
                    }

                    System.out.println(productTitle);
//                    System.out.println(imageProduct);
//                    System.out.println(originalPrice);
//                    System.out.println(discountPercentage);
//                    System.out.println(discountedPrice);
//                    System.out.println(urlLink);
//                    System.out.println(reviewQuantity);
                    System.out.println(soldQuantity);

//                    Product product = new Product();
//                    product.setName(productTitle);
//                    product.setImage(imageProduct);
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
