package com.example.springmcpserver;

import com.example.springmcpserver.service.ToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class SpringMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMcpServerApplication.class, args);
    }


    @Bean
    public ToolCallbackProvider tools(ToolsService toolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolsService).build();
    }
}
