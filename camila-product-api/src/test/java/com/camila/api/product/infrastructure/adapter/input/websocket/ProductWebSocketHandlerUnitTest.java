package com.camila.api.product.infrastructure.adapter.input.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductWebSocketHandler] Product WebSocket Handler Unit Tests")
class ProductWebSocketHandlerUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private WebSocketSession session;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  private ProductWebSocketHandler handler;
  private WebSocketMessage inputMessage;
  private List<String> capturedResponses;
  private AtomicBoolean completed;

  private static Stream<Arguments> sortProductsParams() {
    // salesUnits, stock, profitMargin, daysInStock, page, size
    return Stream.of(
      arguments("0.0", "1.0", "0.0", "0.0", "0", "10"),
      arguments("0.5", "0.5", "0.5", "0.5", "1", "20"),
      arguments("0.8", "0.2", "0.0", "0.0", "2", "50"),
      arguments("0.3", "0.7", "0.0", "0.0", "5", "15"));
  }

  @BeforeEach
  void setUp() {
    this.handler = new ProductWebSocketHandler(this.productUseCase, this.objectMapper);
    this.inputMessage = mock(WebSocketMessage.class);
    this.capturedResponses = new ArrayList<>();
    this.completed = new AtomicBoolean(false);

    // Setup session behaviors that are common to all tests
    when(this.session.send(any())).thenAnswer(invocation -> {
      final Flux<WebSocketMessage> flux = invocation.getArgument(0);
      flux.doOnNext(msg -> this.capturedResponses.add(msg.getPayloadAsText()))
        .doOnComplete(() -> this.completed.set(true)).subscribe();
      return Mono.empty();
    });

    when(this.session.textMessage(anyString())).thenAnswer(invocation -> {
      final var msg = mock(WebSocketMessage.class);
      when(msg.getPayloadAsText()).thenReturn(invocation.getArgument(0));
      return msg;
    });
  }

  @Test
  @DisplayName("Should handle WebSocket session with JSON processing exception")
  void shouldHandleWebSocketSessionWithJsonProcessingException() throws Exception {
    // Given
    final var inputPayload = "{invalid json}";
    this.setupInputMessage(inputPayload);

    // When
    this.executeHandlerAndAwaitCompletion();

    // Then
    verify(this.objectMapper).readTree(inputPayload);
    assertThat(this.capturedResponses).hasSize(1);
    assertThat(this.capturedResponses.getFirst()).startsWith("Error converting request to Json:");
  }

  @Test
  @DisplayName("Should handle find by internal ID WebSocket message")
  void shouldHandleFindByInternalIdMessage() throws Exception {
    // Given
    final var mockProduct = Instancio.of(Product.class).create();
    final var internalId = "123";
    final var inputPayload = """
      {
        "method": "FIND_BY_INTERNAL_ID",
        "internalId": "%s"
      }
      """.formatted(internalId);

    this.setupInputMessage(inputPayload);
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(mockProduct));

    // When
    this.executeHandlerAndAwaitCompletion();

    // Then
    verify(this.productUseCase).findByInternalId(internalId);
    verify(this.objectMapper).writeValueAsString(mockProduct);
    assertThat(this.capturedResponses).hasSize(1);
    assertThat(this.capturedResponses.getFirst()).isEqualTo(this.objectMapper.writeValueAsString(mockProduct));
  }

  @Test
  @DisplayName("Should handle product not found for find by internal ID")
  void shouldHandleProductNotFoundForFindByInternalId() {
    // Given
    final var internalId = "nonexistent";
    final var inputPayload = """
      {
        "method": "FIND_BY_INTERNAL_ID",
        "internalId": "%s"
      }
      """.formatted(internalId);

    this.setupInputMessage(inputPayload);
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When
    this.executeHandlerAndAwaitCompletion();

    // Then
    verify(this.productUseCase).findByInternalId(internalId);
    assertThat(this.capturedResponses).hasSize(1);
    assertThat(this.capturedResponses.getFirst()).isEqualTo("Product not found");
  }

  @ParameterizedTest(name = "{index} -> salesUnits={0}, stock={1}, profitMargin={2}, daysInStock={3}, page={4}, size={5}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should handle sort products message with different parameters")
  void shouldHandleSortProductsMessage(final String salesUnits, final String stock, final String profitMargin,
                                       final String daysInStock, final String page, final String size) throws Exception {
    // Given
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var inputPayload = """
      {
        "method": "SORT_PRODUCTS",
        "salesUnits": "%s",
        "stock": "%s",
        "profitMargin": "%s",
        "daysInStock": "%s",
        "page": "%s",
        "size": "%s"
      }
      """.formatted(salesUnits, stock, profitMargin, daysInStock, page, size);
    final var expectedParams = Map.of("salesUnits", salesUnits, "stock", stock, "profitMargin", profitMargin,
      "daysInStock", daysInStock, "page", page, "size", size);

    this.setupInputMessage(inputPayload);
    when(this.productUseCase.sortByMetricsWeights(expectedParams)).thenReturn(Flux.just(product1, product2));

    // When
    this.executeHandlerAndAwaitCompletion();

    // Then
    verify(this.productUseCase).sortByMetricsWeights(expectedParams);
    verify(this.objectMapper).writeValueAsString(product1);
    verify(this.objectMapper).writeValueAsString(product2);
    assertThat(this.capturedResponses).hasSize(2).containsExactly(this.objectMapper.writeValueAsString(product1),
      this.objectMapper.writeValueAsString(product2));
  }

  @Test
  @DisplayName("Should handle no products found when sorting")
  void shouldHandleNoProductsFoundWhenSorting() {
    // Given
    final var inputPayload = """
      {
        "method": "SORT_PRODUCTS"
      }
      """;
    final var expectedParams = Map.of("salesUnits", "0.0000000001", "stock", "0.0000000001", "profitMargin",
      "0.0000000001", "daysInStock", "0.0000000001", "page", "0", "size", "25");

    this.setupInputMessage(inputPayload);
    when(this.productUseCase.sortByMetricsWeights(expectedParams)).thenReturn(Flux.empty());

    // When
    this.executeHandlerAndAwaitCompletion();

    // Then
    verify(this.productUseCase).sortByMetricsWeights(expectedParams);
    assertThat(this.capturedResponses).hasSize(1);
    assertThat(this.capturedResponses.getFirst()).isEqualTo("No products found");
  }

  private void setupInputMessage(final String payload) {
    when(this.inputMessage.getPayloadAsText(StandardCharsets.UTF_8)).thenReturn(payload);
    when(this.session.receive()).thenReturn(Flux.just(this.inputMessage));
  }

  private void executeHandlerAndAwaitCompletion() {
    this.handler.handle(this.session).subscribe();
    await().atMost(2, SECONDS).untilTrue(this.completed);
  }
}
