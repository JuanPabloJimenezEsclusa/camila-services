package com.camila.api.product.framework.adapter.input.rest.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Open api config.
 */
@Configuration
class OpenAPIConfig {
  @Bean
  OpenAPI springShopOpenAPI() {
    return new OpenAPI()
      .info(new Info().title("Camila Product API")
        .description("API Rest in Camila Product project")
        .version("1.0.0")
        .license(new License().name("GPL-3.0")
          .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/LICENSE.md")))
      .externalDocs(new ExternalDocumentation()
        .description("Camila Service Documentation")
        .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/Readme.md"));
  }
}
