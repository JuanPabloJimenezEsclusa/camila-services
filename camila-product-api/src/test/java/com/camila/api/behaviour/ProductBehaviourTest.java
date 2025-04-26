package com.camila.api.behaviour;

import java.util.List;

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
import org.springframework.test.web.reactive.server.WebTestClient;

@SuppressWarnings("java:S2187")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductBehaviourTest {
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
      table.cell(1, 3));
  }

  @When("^consult products sort and paginated$")
  public void consultProductsSortAndPaginated() {
    exchange = webClient.get().uri("/products?salesUnits={salesUnits}&stock={stock}&page={page}&size={size}", parameters.toArray())
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange();
  }

  @Then("^receive status$")
  public void receiveStatus(final DataTable table) {
    exchange.expectStatus().isEqualTo(Integer.parseInt(table.cell(1, 0)));
  }

  @And("^get sorted data$")
  public void getSortedData(final DataTable table) {
    var body = exchange.expectBody();

    table.asMaps().forEach(element -> {
      var index = element.get("index");
      body
        .jsonPath("$[" + index + "].internalId").isEqualTo(element.get("internalId"))
        .jsonPath("$[" + index + "].salesUnits").isEqualTo(element.get("salesUnits"))
        .jsonPath("$[" + index + "].stock['S']").isEqualTo(element.get("stock_S"))
        .jsonPath("$[" + index + "].stock['M']").isEqualTo(element.get("stock_M"))
        .jsonPath("$[" + index + "].stock['L']").isEqualTo(element.get("stock_L"));
    });
  }

  @And("empty body")
  public void emptyBody() {
    exchange.expectBody().jsonPath("$").isEmpty();
  }

  @And("no body")
  public void noBody() {
    exchange.expectBody().jsonPath("$").doesNotExist();
  }
}
