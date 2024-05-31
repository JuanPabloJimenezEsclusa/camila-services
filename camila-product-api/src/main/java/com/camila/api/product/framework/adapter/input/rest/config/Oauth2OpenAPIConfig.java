package com.camila.api.product.framework.adapter.input.rest.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The type Oauth 2 open api config.
 */
@OpenAPIDefinition(
  servers = {
    @Server(
      url = "http://gateway:8090/product-dev/api",
      description = "Camila Product API (DEV)",
      variables = {
        @ServerVariable(name = "server.host", defaultValue = "gateway", description = "Server Host"),
        @ServerVariable(name = "server.port", defaultValue = "8090", description = "Server Port")
      })
  },
  info = @Info(
    title = "Camila Product API (DEV)",
    description = "API Rest in Camila Product project (docker-compose)",
    contact = @Contact(
      name = "Juan Pablo Jimenez Esclusa",
      email = "juan.pablo.jimenez,esclusa@gmail.com"),
    summary = "Camila Product API",
    version = "1.0.0",
    license = @License(
      name = "GPL-3.0",
      url = "https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/LICENSE.md")),
  externalDocs = @ExternalDocumentation(
    description = "Camila Service Documentation",
    url = "https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/main/Readme.md"),
  security = @SecurityRequirement(name = "camila-client"))

@SecurityScheme(
  name = "camila-client",
  type = SecuritySchemeType.OAUTH2,
  flows = @OAuthFlows(clientCredentials = @OAuthFlow(
    tokenUrl = "${openapi.oAuthFlow.tokenUrl}",
    authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}",
    refreshUrl = "${openapi.oAuthFlow.refreshUrl}",
    scopes = {
      @OAuthScope(name = "openid", description = "openid scope"),
      @OAuthScope(name = "camila.read", description = "read scope"),
      @OAuthScope(name = "camila.write", description = "write scope") })))

@Configuration
@Profile("!default&!loc")
class Oauth2OpenAPIConfig { }
