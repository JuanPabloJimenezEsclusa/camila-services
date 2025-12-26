package com.camila.api.behaviour;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

@SuppressWarnings({"java:S2187", "checkstyle:javadocmethod"})
@CucumberContextConfiguration
@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  properties = {"repository.technology=mongo"})
public class ProductBehaviourTest {
  private static final String SORT_PRODUCT_URI = "/products?salesUnits="
    + "{salesUnits}&stock={stock}&profitMargin={profitMargin}&daysInStock={daysInStock}&page={page}&size={size}";
  private static List<String> parameters = List.of();
  private static WebTestClient.ResponseSpec exchange = null;

  @Autowired
  private WebTestClient webClient;

  @Given("^some metrics weights and page config$")
  public void someMetricsWeightsAndPageConfig(final DataTable table) {
    parameters = List.of(
      table.cell(1, 0),
      table.cell(1, 1),
      table.cell(1, 2),
      table.cell(1, 3),
      table.cell(1, 4),
      table.cell(1, 5));
  }

  @When("^consult products sort and paginated$")
  public void consultProductsSortAndPaginated() {
    exchange = this.webClient.get().uri(SORT_PRODUCT_URI, parameters.toArray())
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange();
  }

  @Then("^receive status$")
  public void receiveStatus(final DataTable table) {
    exchange.expectStatus().isEqualTo(Integer.parseInt(table.cell(1, 0)));
  }

  @And("^get sorted data$")
  public void getSortedData(final DataTable table) {
    final var body = exchange.expectBody();

    table.asMaps().forEach(element -> {
      final var index = element.get("index");
      body.jsonPath("$[%s].internalId".formatted(index)).isEqualTo(element.get("internalId"))
        .jsonPath("$[%s].salesUnits".formatted(index)).isEqualTo(element.get("salesUnits"))
        .jsonPath("$[%s].stock['S']".formatted(index)).isEqualTo(element.get("stock_S"))
        .jsonPath("$[%s].stock['M']".formatted(index)).isEqualTo(element.get("stock_M"))
        .jsonPath("$[%s].stock['L']".formatted(index)).isEqualTo(element.get("stock_L"))
        .jsonPath("$[%s].profitMargin".formatted(index)).isEqualTo(element.get("profitMargin"))
        .jsonPath("$[%s].daysInStock".formatted(index)).isEqualTo(element.get("daysInStock"));
    });
  }

  @And("no body")
  public void noBody() {
    exchange.expectBody().jsonPath("$").doesNotExist();
  }

  @And("error body")
  public void errorBody(final DataTable table) {
    final var body = exchange.expectBody();

    table.asMaps().forEach(element -> body.jsonPath("$.type").isEqualTo(element.get("type")).jsonPath("$.title")
      .isEqualTo(element.get("title")).jsonPath("$.status").isEqualTo(element.get("status"))
      .jsonPath("$.detail").isEqualTo(element.get("detail")).jsonPath("$.instance")
      .value(value -> assertThat(value.toString()).matches(element.get("instance"))).jsonPath("$.errors")
      .value(value -> assertThat(Objects.requireNonNullElse(value, Map.of())).isEqualTo(Map.of())));
  }

  /*
   * When spring context init, it will start a mongo container. This class is used
   * to force the initialization of the MongoContainerConfig class. Without this,
   * the mongo container will not start.
   */
  @Component
  public static class TestContainerConfig extends MongoContainerConfig {
  }
}
