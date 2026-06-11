package com.example.taskflow.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI taskAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")
                        )
                )
                .info(new Info()
                        .title("TaskFlow API")
                        .description("""
                                Enterprise task processing system API
                                Features:
                                 - Async task processing
                                 - Retry mechanism - Scheduler based execution
                                 - Audit tracking
                                 - Task statistics
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Task flow team")
                                .email("taskflow.support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                        )
                ).externalDocs(new ExternalDocumentation()
                        .description("Project documentation")
                        .url("https.//github.com/")
                );
    }
}
