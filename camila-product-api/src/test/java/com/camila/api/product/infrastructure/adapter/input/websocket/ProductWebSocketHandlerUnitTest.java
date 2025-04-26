package com.camila.api.product.infrastructure.adapter.input.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
  private ObjectMapper objectMapper;

  @Mock
  private WebSocketSession session;

  private ProductWebSocketHandler handler;
  private WebSocketMessage inputMessage;
  private List<String> capturedResponses;
  private AtomicBoolean completed;

  private static Stream<Arguments> sortProductsParams() {
    return Stream.of(
      Arguments.of("0.0", "1.0", "0", "10"),
      Arguments.of("0.5", "0.5", "1", "20"),
      Arguments.of("0.8", "0.2", "2", "50"),
      Arguments.of("0.3", "0.7", "5", "15")
    );
  }

  @BeforeEach
  void setUp() {
    handler = new ProductWebSocketHandler(productUseCase, objectMapper);
    inputMessage = mock(WebSocketMessage.class);
    capturedResponses = new ArrayList<>();
    completed = new AtomicBoolean(false);

    // Setup session behaviors that are common to all tests
    when(session.send(any())).thenAnswer(invocation -> {
      final Flux<WebSocketMessage> flux = invocation.getArgument(0);
      flux.doOnNext(msg -> capturedResponses.add(msg.getPayloadAsText()))
        .doOnComplete(() -> completed.set(true))
        .subscribe();
      return Mono.empty();
    });

    when(session.textMessage(anyString())).thenAnswer(invocation -> {
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
    setupInputMessage(inputPayload);

    when(objectMapper.readTree(inputPayload)).thenThrow(new JsonProcessingException("Test error") {
    });

    // When
    executeHandlerAndAwaitCompletion();

    // Then
    verify(objectMapper).readTree(inputPayload);
    assertThat(capturedResponses).hasSize(1);
    assertThat(capturedResponses.getFirst()).startsWith("Error converting request to Json:");
  }

  @Test
  @DisplayName("Should handle find by internal ID WebSocket message")
  void shouldHandleFindByInternalIdMessage() throws Exception {
    // Given
    final var rootNode = setupJsonNodeHierarchy("FIND_BY_INTERNAL_ID");
    final var idNode = mock(JsonNode.class);
    final var mockProduct = Instancio.of(Product.class).create();
    final var internalId = "123";
    final var inputPayload = """
      {
        "method": "FIND_BY_INTERNAL_ID",
        "internalId": "%s"
      }
      """.formatted(internalId);
    final var expectedJson = """
      {
        "product": "data"
      }
      """;

    setupInputMessage(inputPayload);
    when(rootNode.get("internalId")).thenReturn(idNode);
    when(idNode.asText()).thenReturn(internalId);
    when(objectMapper.readTree(inputPayload)).thenReturn(rootNode);
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(mockProduct));
    when(objectMapper.writeValueAsString(mockProduct)).thenReturn(expectedJson);

    // When
    executeHandlerAndAwaitCompletion();

    // Then
    verify(productUseCase).findByInternalId(internalId);
    verify(objectMapper).writeValueAsString(mockProduct);
    assertThat(capturedResponses).hasSize(1);
    assertThat(capturedResponses.getFirst()).isEqualTo(expectedJson);
  }

  @Test
  @DisplayName("Should handle product not found for find by internal ID")
  void shouldHandleProductNotFoundForFindByInternalId() throws Exception {
    // Given
    final var rootNode = setupJsonNodeHierarchy("FIND_BY_INTERNAL_ID");
    final var idNode = mock(JsonNode.class);
    final var internalId = "nonexistent";
    final var inputPayload = "{\"method\":\"FIND_BY_INTERNAL_ID\",\"internalId\":\"" + internalId + "\"}";

    setupInputMessage(inputPayload);
    when(rootNode.get("internalId")).thenReturn(idNode);
    when(idNode.asText()).thenReturn(internalId);
    when(objectMapper.readTree(inputPayload)).thenReturn(rootNode);
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When
    executeHandlerAndAwaitCompletion();

    // Then
    verify(productUseCase).findByInternalId(internalId);
    assertThat(capturedResponses).hasSize(1);
    assertThat(capturedResponses.getFirst()).isEqualTo("Product not found");
  }

  @ParameterizedTest(name = "salesUnits={0}, stock={1}, page={2}, size={3}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should handle sort products message with different parameters")
  void shouldHandleSortProductsMessage(final String salesUnits, final String stock,
                                       final String page, final String size) throws Exception {
    // Given
    final var rootNode = setupJsonNodeHierarchy("SORT_PRODUCTS");
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var inputPayload = """
      {
        "method": "SORT_PRODUCTS",
        "salesUnits": "%s",
        "stock": "%s",
        "page": "%s",
        "size": "%s"
      }
      """.formatted(salesUnits, stock, page, size);
    final var product1Json = """
      {
        "product1": "data"
      }
      """;
    final var product2Json = """
      {
        "product2": "data"
      }
      """;
    final var expectedParams = Map.of(
      "salesUnits", salesUnits,
      "stock", stock,
      "page", page,
      "size", size
    );

    setupInputMessage(inputPayload);
    setupSortNodesWithValues(rootNode, salesUnits, stock, page, size);
    when(objectMapper.readTree(inputPayload)).thenReturn(rootNode);
    when(productUseCase.sortByMetricsWeights(expectedParams)).thenReturn(Flux.just(product1, product2));
    when(objectMapper.writeValueAsString(product1)).thenReturn(product1Json);
    when(objectMapper.writeValueAsString(product2)).thenReturn(product2Json);

    // When
    executeHandlerAndAwaitCompletion();

    // Then
    verify(productUseCase).sortByMetricsWeights(expectedParams);
    verify(objectMapper).writeValueAsString(product1);
    verify(objectMapper).writeValueAsString(product2);
    assertThat(capturedResponses).hasSize(2).containsExactly(product1Json, product2Json);
  }

  @Test
  @DisplayName("Should handle no products found when sorting")
  void shouldHandleNoProductsFoundWhenSorting() throws Exception {
    // Given
    final var rootNode = setupJsonNodeHierarchy("SORT_PRODUCTS");
    final var inputPayload = """
      {
        "method": "SORT_PRODUCTS"
      }
      """;
    final var expectedParams = Map.of(
      "salesUnits", "0.001",
      "stock", "0.999",
      "page", "0",
      "size", "25"
    );

    setupInputMessage(inputPayload);
    setupSortNodesWithValues(rootNode, "0.001", "0.999", "0", "25");
    when(objectMapper.readTree(inputPayload)).thenReturn(rootNode);
    when(productUseCase.sortByMetricsWeights(expectedParams)).thenReturn(Flux.empty());

    // When
    executeHandlerAndAwaitCompletion();

    // Then
    verify(productUseCase).sortByMetricsWeights(expectedParams);
    assertThat(capturedResponses).hasSize(1);
    assertThat(capturedResponses.getFirst()).isEqualTo("No products found");
  }

  private void setupInputMessage(final String payload) {
    when(inputMessage.getPayloadAsText(StandardCharsets.UTF_8)).thenReturn(payload);
    when(session.receive()).thenReturn(Flux.just(inputMessage));
  }

  private JsonNode setupJsonNodeHierarchy(final String method) {
    final var rootNode = mock(JsonNode.class);
    final var methodNode = mock(JsonNode.class);

    when(rootNode.get("method")).thenReturn(methodNode);
    when(methodNode.asText(anyString())).thenReturn(method);
    return rootNode;
  }

  private void executeHandlerAndAwaitCompletion() {
    handler.handle(session).subscribe();
    await().atMost(2, SECONDS).untilTrue(completed);
  }

  private void setupSortNodesWithValues(final JsonNode rootNode, final String salesUnits, final String stock,
                                        final String page, final String size) {
    final var salesNode = mock(JsonNode.class);
    final var stockNode = mock(JsonNode.class);
    final var pageNode = mock(JsonNode.class);
    final var sizeNode = mock(JsonNode.class);

    when(rootNode.get("salesUnits")).thenReturn(salesNode);
    when(rootNode.get("stock")).thenReturn(stockNode);
    when(rootNode.get("page")).thenReturn(pageNode);
    when(rootNode.get("size")).thenReturn(sizeNode);

    when(salesNode.asText(anyString())).thenReturn(salesUnits);
    when(stockNode.asText(anyString())).thenReturn(stock);
    when(pageNode.asText(anyString())).thenReturn(page);
    when(sizeNode.asText(anyString())).thenReturn(size);
  }
}
