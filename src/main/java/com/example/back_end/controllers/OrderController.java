package com.example.back_end.controllers;

import com.example.back_end.models.Order;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final RestTemplate restTemplate;

    String token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxNjkyNzYyNSIsImlhdCI6MTcyMzA0Mjg2NCwiZXhwIjoxNzIzMTI5MjY0LCJpc3MiOiJodHRwczovL3Rpa2kudm4iLCJjdXN0b21lcl9pZCI6IjE2OTI3NjI1IiwiZW1haWwiOiJ0YW5waHVvbmcyMzA3QGdtYWlsLmNvbSIsImNsaWVudF9pZCI6InRpa2ktc3NvIiwibmFtZSI6IjE2OTI3NjI1Iiwic2NvcGUiOiJzc28iLCJncmFudF90eXBlIjoiZ29vZ2xlIn0.xHHRzo5cXbhIwQWguGHYUmglWrcmEO6tIVI-zdM3tyO7PRDuvz5u1CHdz-98QbJAGsYU5cM77ddB2t0-g4wixH234_hAVVh0gpkz8YoLGsjNVtwf7vbGkZ2u5bvhMEmrKtGDVpnutabDIJ4pObqFwHgF57yubgZJ8Q-HdJptWl8dynLMYvN8jSf1VYohX085rIHF6Tz4Z_LI4aywBapiiq3gL7_BmtR52CRShJb4P4C_9OcXCAcZvPmM4iITkZE7ymDwBekAK4DmcZwSB_WlaIIfv6WVEsTS-e1fcHCzEC4LNmBowx9pCZXQNHffPK-yZNn3EDzzHty0BBz4qlbm2KFGODTu7Xs_tkrRK6xJQd2PeyuL2qCLJwxjwZ1kFidLUtoDignQFzVm7KgBb-5H2nty2fiB5gk1d_SMG9tZtLHhStnnHLA6yNEEaYzI6IANSXhTNsWySI4x4feD3WHOwfwqKuVOK5jhddPHkDw1Y8esq8szKNRp5FmjQsanuyFNF3Su-YnZca5SZlbi8ZENmlbMF136kfFN8N2vI7DohjjoHA-2CrUlJmbYL7R4HBvW7XMFg-YPX9J9N7aqpSGiSGfQ5qNJlrOwQwM7IufALBKmed5ae9k2v3NIj7EU03NMk2Ys7OVfs29sTBvElKrNUFEBH2Io9dor4uhN6qqimSc";

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

            System.out.println(requestJson);

            String getAPI = "https://api.tiki.vn/raiden/v2/best-price/products/" + spidValue;

            // Cấu hình headers cho yêu cầu
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", token);
            headers.set("X-Requested-With", "XMLHttpRequest");
            headers.set("Cache-Control", "no-cache");
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = null;
            ResponseEntity<String> responseGet = null;

            try {
                response = template.postForEntity(url, requestEntity, String.class);
                System.out.println("Response Status Code: " + response.getStatusCode());
                System.out.println("Response Body: " + response.getBody());

                responseGet = template.exchange(getAPI, HttpMethod.GET, requestEntity, String.class);
                System.out.println("Response Status Code: " + responseGet.getStatusCode());
                System.out.println("Response Body: " + responseGet.getBody());
            } catch (HttpClientErrorException e) {
                System.err.println("HTTP Status Code: " + e.getStatusCode());
                System.err.println("Response Body: " + e.getResponseBodyAsString());
                return ResponseEntity.badRequest().body("Error: " + e.getResponseBodyAsString());
            }

            return ResponseEntity.ok("Created order");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
