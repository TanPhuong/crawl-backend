package com.example.back_end.controllers;

import com.example.back_end.dtos.ProductDTO;
import com.example.back_end.models.Product;
import com.example.back_end.services.TaskService;
import com.example.back_end.services.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TaskController {

    private TaskService taskService;
    private WorkerService workerService;

    public TaskController(TaskService taskService, WorkerService workerService) {
        this.taskService = taskService;
        this.workerService = workerService;
    }

    @MutationMapping
    public String createTasks(@Argument(name = "productInput") Product productDTO) {
        this.taskService.createAndPushTasks(productDTO);
        this.workerService.processTasks();
        return "Complete task";
    }

}
