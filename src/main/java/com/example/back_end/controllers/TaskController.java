package com.example.back_end.controllers;

import com.example.back_end.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TaskController {

    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


}
