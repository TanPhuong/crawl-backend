package com.example.back_end.services;

import com.example.back_end.models.*;
import com.example.back_end.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WorkerService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_STREAM = "task-stream";
    private static final String CONSUMER_GROUP = "task-group";
    private static final String CONSUMER_NAME = "worker-1";

    private OrderRepository orderRepository;

    public WorkerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void init() {
        createConsumerGroup();
    }

    private void createConsumerGroup() {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        try {
            // Tạo Consumer Group nếu không tồn tại
            streamOps.createGroup(TASK_STREAM, CONSUMER_GROUP);
            System.out.println("Consumer Group created successfully.");
        } catch (Exception e) {
            if (e.getMessage().contains("BUSYGROUP")) {
                System.out.println("Consumer Group already exists.");
            } else {
                e.printStackTrace();
            }
        }
    }

    @Async
    public void processTasks() {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();

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

                Crawl crawlOrder = new Crawl();
                crawlOrder.setId((Long) taskData.get("productCrawlId"));
                crawlOrder.setNameUrl((String) taskData.get("productCrawlUrl"));
                crawlOrder.setStatus((Boolean) taskData.get("productCrawlStatus"));

                Role roleOrder = new Role();
                roleOrder.setId((Long) taskData.get("userRoleId"));
                roleOrder.setName((String) taskData.get("userRoleName"));

                Product productOrder = Product.builder()
                        .id((Long) taskData.get("productId"))
                        .name((String) taskData.get("productName"))
                        .price((Float) taskData.get("productPrice"))
                        .discount((Float) taskData.get("productDiscount"))
                        .salePrice((Float) taskData.get("productSalePrice"))
                        .review((Float) taskData.get("productReview"))
                        .sold((Float) taskData.get("productSold"))
                        .url((String) taskData.get("productUrl"))
                        .image((String) taskData.get("productImg"))
                        .crawl(crawlOrder)
                        .build();

                User userOrder = User.builder()
                        .id((Long) taskData.get("userId"))
                        .fullName((String) taskData.get("username"))
                        .email((String) taskData.get("userEmail"))
                        .phoneNumber((Long) taskData.get("userPhone"))
                        .build();


                Order newOrder = new Order();
                newOrder.setCreateAt(LocalDateTime.now());
                newOrder.setProduct(productOrder);
                newOrder.setUser(userOrder);
                this.orderRepository.save(newOrder);

                // Xác nhận đã xử lý task
                streamOps.acknowledge(TASK_STREAM, CONSUMER_GROUP, taskId);
            }
        }
    }
}
