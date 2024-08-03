package com.example.back_end.services;

import com.example.back_end.models.Product;
import com.example.back_end.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_STREAM = "task-stream";

    public void createAndPushTasks(Product productInput, User userInput) {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        for (int i = 0; i < 50; i++) {

            // Tạo một Map để chứa các trường và giá trị của bản ghi
            Map<String, String> fields = new HashMap<>();
            fields.put("productId", String.valueOf(productInput.getId()));
            fields.put("productName", productInput.getName());
            fields.put("productPrice", String.valueOf(productInput.getPrice()));
            fields.put("productDiscount", String.valueOf(productInput.getDiscount()));
            fields.put("productSalePrice", String.valueOf(productInput.getSalePrice()));
            fields.put("productReview", String.valueOf(productInput.getReview()));
            fields.put("productSold", String.valueOf(productInput.getSold()));
            fields.put("productUrl", productInput.getUrl());
            fields.put("productImg", productInput.getImage());
            fields.put("productCrawlId", String.valueOf(productInput.getCrawl().getId()));
            fields.put("productCrawlUrl", productInput.getCrawl().getNameUrl());
            fields.put("productCrawlStatus", String.valueOf(productInput.getCrawl().getStatus()));

            fields.put("userId", String.valueOf(userInput.getId()));
            fields.put("userEmail", userInput.getEmail());
            fields.put("username", userInput.getFullName());
            fields.put("userPhone", String.valueOf(userInput.getPhoneNumber()));
            fields.put("userRoleId", String.valueOf(userInput.getRole().getId()));
            fields.put("userRoleName", userInput.getRole().getName());

            // Tạo bản ghi từ Map và thêm nó vào stream
            MapRecord<String, String, String> record = MapRecord.create(TASK_STREAM, fields);
            RecordId recordId = streamOps.add(record);

            System.out.println("Added task with ID: " + recordId);
        }
    }
}
