package com.camila.api.product.framework.adapter.input.rest.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The type Oauth2 open api config.
 */
@OpenAPIDefinition(
  servers = {
    @Server(
      url = "${openapi.oAuthFlow.url}${spring.webflux.base-path}", // not add path separator
      description = "Camila Product API")
  },
  info = @Info(
    title = "Camila Product API",
    description = "API Rest in Camila Product project",
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
  flows = @OAuthFlows(
    // used into swagger-ui
    clientCredentials = @OAuthFlow(
      tokenUrl = "${openapi.oAuthFlow.tokenUrl}",
      authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}",
      refreshUrl = "${openapi.oAuthFlow.refreshUrl}",
      scopes = {
        @OAuthScope(name = "camila/read", description = "read scope"),
        @OAuthScope(name = "camila/write", description = "write scope") }),
    // used into postman, cli and swagger-ui
    authorizationCode = @OAuthFlow(
      tokenUrl = "${openapi.oAuthFlow.tokenUrl}",
      authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}",
      refreshUrl = "${openapi.oAuthFlow.refreshUrl}",
      scopes = {
        @OAuthScope(name = "openid", description = "openid scope"),
        @OAuthScope(name = "camila/read", description = "read scope"),
        @OAuthScope(name = "camila/write", description = "write scope") }),
    // supported by Keycloak but not by AWS Cognito
    password = @OAuthFlow(
      tokenUrl = "${openapi.oAuthFlow.tokenUrl}",
      authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}",
      refreshUrl = "${openapi.oAuthFlow.refreshUrl}",
      scopes = {
        @OAuthScope(name = "openid", description = "openid scope"),
        @OAuthScope(name = "camila/read", description = "read scope"),
        @OAuthScope(name = "camila/write", description = "write scope") })
  ))

@Configuration
@Profile("dev|pre")
class Oauth2OpenAPIConfig { }
