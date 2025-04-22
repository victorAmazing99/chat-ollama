package com.example.springmcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ToolsService {

    @Tool(description = "获取当前时间")
    public String getTime(){
        System.out.println("=============");
        return new Date().toString();
    }
}
