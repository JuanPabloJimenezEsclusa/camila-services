package com.camila.api.product.infrastructure.adapter.input.rest.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The type Open api config.
 */
@Configuration(proxyBeanMethods = false)
@Profile("default|loc|local-compose|int|pro")
public class LocalOpenAPIConfig {
  @Bean
  OpenAPI springOpenAPI() {
    return new OpenAPI()
      .info(new Info().title("Camila Product API")
        .description("API Rest in Camila Product project")
        .summary("Camila Product API")
        .contact(new Contact()
          .name("Juan Pablo Jimenez Esclusa")
          .email("juan.pablo.jimenez,esclusa@gmail.com"))
        .version("1.0.0")
        .license(new License().name("GPL-3.0")
          .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/LICENSE.md")))
      .externalDocs(new ExternalDocumentation()
        .description("Camila Service Documentation")
        .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/Readme.md"));
  }
}
