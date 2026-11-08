package com.dsaquest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI dsaQuestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DSA QUEST API")
                        .version("v1")
                        .description("Backend API foundation for DSA QUEST."));
    }
}
