package com.example.back_end.controllers;

import com.example.back_end.models.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    String token = "123";

    @Autowired
    public OrderController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody Order orderDTO,
            BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }


            String url = "https://tiki.vn/api/v2/carts/mine/buy-now";

            String productUrl = orderDTO.getProduct().getUrl();

            URI uri = new URI(productUrl);

            String query = uri.getQuery();

            String[] pairs = query.split("&");
            Long spidValue = null;

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue[0].equals("spid")) {
                    spidValue = Long.parseLong(keyValue[1]);
                    break;
                }
            }

            // Tạo JSON data để gửi trong body
            String requestJson = String.format(
                    "{\"coupons\": [], \"products\": [{\"product_id\": %d, \"quantity\": 1, \"add_on_products\": []}]}",
                    spidValue
            );

//            System.out.println(requestJson);

            String getAPI = "https://api.tiki.vn/raiden/v2/best-price/products/" + spidValue;

            // Cấu hình headers cho yêu cầu
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("token", token);
            headers.set("X-Requested-With", "XMLHttpRequest");
            headers.set("Cache-Control", "no-cache");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> responseGet = null;

            try {
//                response = template.postForEntity(url, requestEntity, String.class);
//                System.out.println("Response Status Code: " + response.getStatusCode());
//                System.out.println("Response Body: " + response.getBody());

                responseGet = template.exchange(getAPI, HttpMethod.GET, requestEntity, String.class);
                System.out.println("Response Status Code: " + responseGet.getStatusCode());
                System.out.println("Response Body: " + responseGet.getBody());

                // Read json data
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseGet.getBody());

                String valueCart = jsonNode.get("add_to_cart").asText();
                System.out.println(valueCart);

                Map<String, String> responseJson = new HashMap<>();
                responseJson.put("add_to_cart", valueCart);

                return ResponseEntity.ok(responseJson);

            } catch (HttpClientErrorException e) {
                System.err.println("HTTP Status Code: " + e.getStatusCode());
                System.err.println("Response Body: " + e.getResponseBodyAsString());
                return ResponseEntity.badRequest().body("Error: " + e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
