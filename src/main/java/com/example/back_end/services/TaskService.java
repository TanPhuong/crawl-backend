package com.example.back_end.services;

import com.example.back_end.models.Product;
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

    public void createAndPushTasks(Product productInput) {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        for (int i = 0; i < 50; i++) {

            // Tạo một Map để chứa các trường và giá trị của bản ghi
            Map<String, String> fields = new HashMap<>();
            fields.put("productId", String.valueOf(productInput.getId()));
            fields.put("productName", productInput.getName());
            fields.put("productPrice", String.valueOf(productInput.getPrice()));

            // Tạo bản ghi từ Map và thêm nó vào stream
            MapRecord<String, String, String> record = MapRecord.create(TASK_STREAM, fields);
            RecordId recordId = streamOps.add(record);

            System.out.println("Added task with ID: " + recordId);
        }
    }

}
