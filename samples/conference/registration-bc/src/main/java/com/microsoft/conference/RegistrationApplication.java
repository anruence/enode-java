package com.microsoft.conference;

import com.microsoft.conference.common.config.ConferenceDataSourceConfiguration;
import com.microsoft.conference.common.config.KafkaEventConfig;
import com.microsoft.conference.common.config.SwaggerConfiguration;
import com.microsoft.conference.common.config.WebMvcConfiguration;
import org.enodeframework.spring.EnableEnode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableEnode
@EnableOpenApi
@Import(value = {
        ConferenceDataSourceConfiguration.class,
        KafkaEventConfig.class,
        WebMvcConfiguration.class,
        SwaggerConfiguration.class
})
public class RegistrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistrationApplication.class, args);
    }
}