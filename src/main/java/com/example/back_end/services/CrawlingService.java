package com.example.back_end.services;

import com.example.back_end.models.Crawl;
import com.example.back_end.models.Keyword;
import com.example.back_end.models.Product;
import com.example.back_end.models.Time;
import com.example.back_end.repository.CrawlRepository;
import com.example.back_end.repository.KeywordRepository;
import com.example.back_end.repository.ProductRepository;
import com.example.back_end.repository.TimeRepository;
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
import java.util.*;
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

    private ProductRepository productRepository;

    private TimeRepository timeRepository;
    private KeywordRepository keywordRepository;

    @Autowired
    public CrawlingService(Playwright playwright, CrawlRepository crawlRepository,
                           ProductRepository productRepository, TimeRepository timeRepository, KeywordRepository keywordRepository) {
        this.crawlRepository = crawlRepository;
        this.playwright = playwright;
        this.productRepository = productRepository;
        this.timeRepository = timeRepository;
        this.keywordRepository = keywordRepository;
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

    public List<Product> crawlProduct(String url, List<String> keywords, Crawl crawlUrl) {

        // Compare entity Keyword and entity Crawl
        Keyword matchCrawl = this.keywordRepository.findCrawlById(crawlUrl.getId());
        System.out.println(matchCrawl);

        // Playwright
        Queue<String> listHref = new LinkedBlockingQueue<>();
        List<Product> productList = new ArrayList<>();

        try {
            // Playwright
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
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
                List<Locator> locators = page.locator(matchCrawl.getKeyword_sale_url()).all();

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
                    List<Locator> saleTimeList = page.locator(matchCrawl.getKeyword_wrapper())
                            .locator(matchCrawl.getKeyword_uptime()).all();

                    if(saleTimeList.isEmpty()) {
                        throw new Exception("No elements found with the specified selector.");
                    }

                    for (Locator saleTimeLocator : saleTimeList) {
                        ElementHandle element = saleTimeLocator.elementHandle();
                        String saleTimeString = element.textContent();
                        LocalTime saleTime = LocalTime.parse(saleTimeString);

                        System.out.println(saleTime);

                        // create crawl entity to save to time in database

                        Time time = new Time();
                        time.setTimeCrawl(saleTime);
                        time.setDateCrawl(LocalDate.now());
                        time.setCrawl(crawlUrl);
                        this.timeRepository.save(time);
                    }
                } catch (Exception e) {
                    System.out.println("Message" + e.getMessage());
                }

                // Get product information

                // Get size of the product container
                List<Locator> productTitleList = page.locator(matchCrawl.getKeyword_title()).all();

                System.out.println(productTitleList.size());

                for (int i = 0; i < productTitleList.size(); i++) {

                    // 1. Name Product
                    String productTitle = page.locator(matchCrawl.getKeyword_wrapper())
                            .locator(matchCrawl.getKeyword_title()).nth(i).textContent();


                    // 2. Product Image
                    String imageProduct = page.locator(matchCrawl.getKeyword_wrapper())
                            .locator(matchCrawl.getKeyword_image()).nth(i).getAttribute("src");

//                    System.out.println(imageProduct);

                    // 3. Original Price
                    Float originalPrice;
                    try {
                        Locator originalPriceLocator = page.locator(matchCrawl.getKeyword_wrapper())
                                .locator(matchCrawl.getKeyword_price()).nth(i);
                        originalPriceLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String originalPriceString = originalPriceLocator.elementHandle().textContent();

                        String convertOriginalPrice = originalPriceString.replaceAll("[^0-9.-]+", "");
                        originalPrice = Float.parseFloat(convertOriginalPrice.replace(".",""));
                    } catch (Exception e) {
                        originalPrice = 0f;
                    }

                    // 4. Sale Percentage
                    Float discountPercentage;
                    try {

                        Locator discountPercentageLocator = page.locator(matchCrawl.getKeyword_wrapper())
                                .locator(matchCrawl.getKeyword_discount()).nth(i);
                        discountPercentageLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String discountPercentageString = discountPercentageLocator.elementHandle().textContent();

                        discountPercentage = Float.parseFloat(discountPercentageString.replaceAll("[^0-9.-]+", ""));

                        if(-20 < discountPercentage && discountPercentage < 20) {
                            continue;
                        }

                    } catch (Exception e) {
                        discountPercentage = 0f;
                    }

                    // 5. Discount price
                    Float discountedPrice;
                    try {
                        Locator discountedPriceLocator = page.locator(matchCrawl.getKeyword_wrapper())
                                .locator(matchCrawl.getKeyword_sale()).nth(i);
                        discountedPriceLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));

                        String discountPercentageString = discountedPriceLocator.elementHandle().textContent();

                        String convertDiscountedPrice = discountPercentageString.replaceAll("[^0-9.-]+", "");
                        discountedPrice = Float.parseFloat(convertDiscountedPrice.replace(".",""));
                    } catch (Exception e) {
                        discountedPrice = 0f;
                    }

                    // 6. Url of Product
                    String urlLink = page.locator(matchCrawl.getKeyword_wrapper())
                            .locator(matchCrawl.getKeyword_product()).nth(i).getAttribute("href");

//                     Create new browser context to add proxy pool when navigate to each product url
//                    page.waitForTimeout(5000);

//                    BrowserContext contextPerPage = browser.newContext(new Browser.NewContextOptions()
//                            .setUserAgent(userAgent)
//                    );
//
//                    Page perPage = contextPerPage.newPage();
//
//                    perPage.navigate(urlLink);
//                    perPage.waitForLoadState(LoadState.LOAD);

                    // 7. Get review quantity
                    Float reviewQuantity = 0f;
//                    try {
//                        perPage.waitForSelector(matchCrawl.getKeyword_review(), new Page.WaitForSelectorOptions().setTimeout(2000));
//
//                        String reviewQuantityString = perPage.locator(matchCrawl.getKeyword_review()).textContent();
//                        String convertReviewQuantity = reviewQuantityString.replaceAll("[^0-9.-]+", "");
//                        reviewQuantity = Float.parseFloat(convertReviewQuantity);
//                    } catch (Exception e) {
//                        reviewQuantity = 0f;
//                    }

                    // 8. Get sold quantity
                    Float soldQuantity = 0f;
//                    try {
//                        perPage.waitForSelector(matchCrawl.getKeyword_sold(), new Page.WaitForSelectorOptions().setTimeout(2000));
//
//                        String soldQuantityString = perPage.locator(matchCrawl.getKeyword_sold()).textContent();
//                        String convertSoldQuantity = soldQuantityString.replaceAll("[^0-9.-]+", "");
//                        soldQuantity = Float.parseFloat(convertSoldQuantity);
//                    } catch (Exception e) {
//                        soldQuantity = 0f;
//                    }

                    System.out.println(productTitle);
                    System.out.println(soldQuantity);

                    Product product = new Product();
                    product.setName(productTitle);
                    product.setImage(imageProduct);
                    product.setPrice(originalPrice);
                    product.setDiscount(discountPercentage);
                    product.setSalePrice(discountedPrice);
                    product.setUrl(urlLink);
                    product.setReview(reviewQuantity);
                    product.setSold(soldQuantity);
                    product.setCrawl(crawlUrl);

                    this.productRepository.save(product);

                    productList.add(product);

//                    perPage.close();

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

    public Keyword writeConfig(Crawl crawl) {

        Keyword keywordConfig = new Keyword();

        // Check if exist config about web crawl
        Keyword checkExistKeyword = this.keywordRepository.findCrawlById(crawl.getId());
        if(checkExistKeyword != null) {
            return checkExistKeyword;
        }

        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions());
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setUserAgent(userAgent)
        );

        Page page = context.newPage();
        // Điều hướng tới url
        page.navigate(crawl.getNameUrl());
        page.waitForLoadState(LoadState.LOAD);

        // Build an automative system to crawl
        ElementHandle elementHandle = page.querySelector("body *");
        // B0: Find keywords to sale page and navigate to it
        String saleUrl = null;
        List<String> foundAttributeHref = new ArrayList<>();

        List<String> urlSelectors = new ArrayList<>();
        urlSelectors.add("deal-hot");
        urlSelectors.add("flash-sale");

        for(String keywordUrl : urlSelectors) {
            Object attributeValues = elementHandle.evaluate("(element, keywordUrl) => { " +
                    "let values = []; " +
                    "for (let attr of element.attributes) { " +
                    "    if (attr.value.includes(keywordUrl)) { " +
                    "        values.push(attr.value); " +
                    "    } " +
                    "} " +
                    "return values; " +
                    "}", keywordUrl);

            if (attributeValues instanceof List<?>) {
                List<?> attributeList = (List<?>) attributeValues;
                for (Object value : attributeList) {
                    if (value instanceof String) {
                        foundAttributeHref.add((String) value);

                        if (!foundAttributeHref.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }

        for(String href : foundAttributeHref) {
            saleUrl = href;
            break;
        }

        System.out.println(saleUrl);
        page.navigate(saleUrl);
        page.waitForLoadState(LoadState.LOAD);

        // B1: Archive those selectors/locators like keywords (entity Keyword)
        Map<String, List<String>> selectors = new HashMap<>();
        selectors.put("wrapper", Arrays.asList("wrapper", "Wrapper"));
        selectors.put("uptime", Arrays.asList("upcoming-time"));
        selectors.put("product_title", Arrays.asList("ProductTitle", "product-title"));

        // B2: Read the structure if keyword match with selectors of the web then scrape the ATTRIBUTE and the TAGNAME
        Map<String, List<String>> matchingSeletors = new HashMap<>();

        // B2.1: Get ATTRIRBUTE
        Map<String, List<String>> foundAttributes = new HashMap<>();

        for(Map.Entry<String, List<String>> entry: selectors.entrySet()) {

            List<String> matchingKeyword = new ArrayList<>();

            String selectorKey = entry.getKey();
            List<String> keywordSelectors = entry.getValue();

            for(String keywordSelector : keywordSelectors) {
                Object attributeValues = elementHandle.evaluate("(element, keywordSelector) => { " +
                        "let values = []; " +
                        "for (let attr of element.attributes) { " +
                        "if (attr.value.includes(keywordSelector)) { " +
                        "values.push(attr.name); " +
                        "} " +
                        "} " +
                        "return values; " +
                        "}", keywordSelector);

                System.out.println(attributeValues);

                // Ép kiểu của attributeValues để lưu vào matchingKeyword
                if(attributeValues instanceof List<?>) {
                    for(Object item: (List<?>) attributeValues) {
                        if (item instanceof String) {

                            String attributeName = (String)  item;
                            String combinedAttribute = attributeName + "*='" +keywordSelector +"'";

                            matchingKeyword.add(combinedAttribute);
                        }
                    }
                }
            }

            if(!matchingKeyword.isEmpty()) {
                foundAttributes.put(selectorKey, matchingKeyword);
            }

            System.out.println(foundAttributes);
        }


        // B2.2: Get TAGNAME
        Map<String, List<String>> foundTagName = new HashMap<>();

        for(Map.Entry<String, List<String>> entry: foundAttributes.entrySet()) {

        }


        // B3: If use textContent() with Attribute and Tagname and got result, set KeywordConfig


        page.close();
        return keywordConfig;
    }

    public Keyword rewriteConfig(Crawl crawl) {
        Keyword reConfig = new Keyword();

        return  reConfig;
    }
}
