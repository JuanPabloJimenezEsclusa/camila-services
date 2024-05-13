package com.camila.gateway.presentation;

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
public class OpenAPIConfig {

  /**
   * Spring shop open api open api.
   *
   * @return the open api
   */
  @Bean
  OpenAPI springShopOpenAPI() {
    return new OpenAPI()
      .info(new Info().title("Camila Gateway API")
        .description("Gateway implementation to Camila Service project")
        .version("1.0.0")
        .license(new License().name("GPL-3.0")
          .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/LICENSE.md")))
      .externalDocs(new ExternalDocumentation()
        .description("Camila Service Documentation")
        .url("https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/Readme.md"));
  }
}
