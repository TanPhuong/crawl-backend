package com.example.back_end.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class WorkerService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_STREAM = "task-stream";
    private static final String CONSUMER_GROUP = "task-group";
    private static final String CONSUMER_NAME = "worker-1";

    @PostConstruct
    public void init() {
        processTasks();
    }

    @Async
    public void processTasks() {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();

        // Tạo Consumer Group nếu chưa có
        try {
            streamOps.createGroup(TASK_STREAM, CONSUMER_GROUP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            // Đọc task từ stream bằng consumer group
            List<MapRecord<String, Object, Object>> tasks = streamOps.read(
                    Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                    StreamReadOptions.empty().count(100).block(Duration.ofSeconds(2)),
                    StreamOffset.create(TASK_STREAM, ReadOffset.lastConsumed())
            );

            for (MapRecord<String, Object, Object> task : tasks) {
                RecordId taskId = task.getId();
                Map<Object, Object> taskData = task.getValue();
                // Xử lý task
                System.out.println("Processing task: " + taskId + " with data: " + taskData);
                // Xác nhận đã xử lý task
                streamOps.acknowledge(TASK_STREAM, CONSUMER_GROUP, taskId);
            }
        }
    }
}
