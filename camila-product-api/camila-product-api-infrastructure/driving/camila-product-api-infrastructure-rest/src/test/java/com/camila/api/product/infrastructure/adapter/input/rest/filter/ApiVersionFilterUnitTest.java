package com.camila.api.product.infrastructure.adapter.input.rest.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ApiVersionFilter] API Version Filter Unit Tests")
class ApiVersionFilterUnitTest {

  private static final String API_VERSION_HEADER = "X-Api-Version";
  private static final String SERVICE_VERSION = "1.0.0";

  private static Stream<Arguments> matchingVersionParams() {
    return Stream.of(
      arguments("1.0.0"),
      arguments("1.0.0"),
      arguments("1.0.0")
    );
  }

  private static Stream<Arguments> mismatchingVersionParams() {
    return Stream.of(
      arguments("2.0.0"),
      arguments("0.9.0"),
      arguments("1.1.0"),
      arguments("3.5.2")
    );
  }

  private static Stream<Arguments> caseInsensitiveVersionParams() {
    return Stream.of(
      arguments("1.0.0", true),
      arguments("1.0.0", true),
      arguments("1.0.0", true),
      arguments("2.0.0", false),
      arguments("0.5.0", false)
    );
  }

  @Mock
  private ServerWebExchange exchange;

  @Mock
  private ServerHttpRequest request;

  @Mock
  private ServerHttpResponse response;

  @Mock
  private HttpHeaders requestHeaders;

  @Mock
  private HttpHeaders responseHeaders;

  @Mock
  private WebFilterChain chain;

  @ParameterizedTest
  @MethodSource("matchingVersionParams")
  @DisplayName("Should allow request when API version matches")
  void shouldAllowRequestWhenApiVersionMatches(final String requestVersion) {
    // Given
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(exchange).getRequest();
    verify(exchange).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);
    verify(chain).filter(exchange);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders, chain);
  }

  @ParameterizedTest
  @MethodSource("mismatchingVersionParams")
  @DisplayName("Should reject request when API version does not match")
  void shouldRejectRequestWhenApiVersionDoesNotMatch(final String requestVersion) {
    // Given
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(exchange).getRequest();
    verify(exchange, times(2)).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);
    verify(response).setStatusCode(HttpStatus.NOT_ACCEPTABLE);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders);
    verifyNoInteractions(chain);
  }

  @Test
  @DisplayName("Should use service version when header is missing")
  void shouldUseServiceVersionWhenHeaderIsMissing() {
    // Given
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(null);
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(exchange).getRequest();
    verify(exchange).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, SERVICE_VERSION);
    verify(chain).filter(exchange);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders, chain);
  }

  @Test
  @DisplayName("Should set response header with request version")
  void shouldSetResponseHeaderWithRequestVersion() {
    // Given
    final var requestVersion = "1.0.0";
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);
    verify(exchange).getRequest();
    verify(exchange).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(chain).filter(exchange);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders, chain);
  }

  @Test
  @DisplayName("Should return empty mono when version mismatches")
  void shouldReturnEmptyMonoWhenVersionMismatches() {
    // Given
    final var requestVersion = "2.0.0";
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(response).setStatusCode(HttpStatus.NOT_ACCEPTABLE);
    verify(exchange).getRequest();
    verify(exchange, times(2)).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders);
    verifyNoInteractions(chain);
  }

  @ParameterizedTest
  @MethodSource("caseInsensitiveVersionParams")
  @DisplayName("Should perform case insensitive version comparison")
  void shouldPerformCaseInsensitiveVersionComparison(final String requestVersion, final boolean shouldPass) {
    // Given
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);
    if (shouldPass) {
      when(chain.filter(exchange)).thenReturn(Mono.empty());
    }

    // When & Then
    filter.filter(exchange, chain)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(exchange).getRequest();
    verify(exchange, times(shouldPass ? 1 : 2)).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);

    if (shouldPass) {
      verify(chain).filter(exchange);
      verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders, chain);
    } else {
      verify(response).setStatusCode(HttpStatus.NOT_ACCEPTABLE);
      verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders);
      verifyNoInteractions(chain);
    }
  }

  @Test
  @DisplayName("Should continue filter chain when versions match")
  void shouldContinueFilterChainWhenVersionsMatch() {
    // Given
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(SERVICE_VERSION);
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    // When
    final var result = filter.filter(exchange, chain);

    // Then
    assertThat(result).isNotNull();
    result.as(StepVerifier::create).verifyComplete();

    verify(chain).filter(exchange);
    verify(exchange).getRequest();
    verify(exchange).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, SERVICE_VERSION);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders, chain);
  }

  @Test
  @DisplayName("Should not continue filter chain when versions mismatch")
  void shouldNotContinueFilterChainWhenVersionsMismatch() {
    // Given
    final var requestVersion = "3.0.0";
    final var filter = new ApiVersionFilter(SERVICE_VERSION);
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getResponse()).thenReturn(response);
    when(request.getHeaders()).thenReturn(requestHeaders);
    when(response.getHeaders()).thenReturn(responseHeaders);
    when(requestHeaders.getFirst(API_VERSION_HEADER)).thenReturn(requestVersion);

    // When
    final var result = filter.filter(exchange, chain);

    // Then
    assertThat(result).isNotNull();
    result.as(StepVerifier::create).verifyComplete();

    verifyNoInteractions(chain);
    verify(response).setStatusCode(HttpStatus.NOT_ACCEPTABLE);
    verify(exchange).getRequest();
    verify(exchange, times(2)).getResponse();
    verify(request).getHeaders();
    verify(response).getHeaders();
    verify(requestHeaders).getFirst(API_VERSION_HEADER);
    verify(responseHeaders).set(API_VERSION_HEADER, requestVersion);
    verifyNoMoreInteractions(exchange, request, response, requestHeaders, responseHeaders);
  }
}

