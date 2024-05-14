package com.camila.api.behaviour;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
  features = "src/test/resources/feature/",
  glue = { "com.camila.api.behaviour" },
  publish = true,
  monochrome = true,
  plugin = {
    "pretty",
    "json:target/cucumber-reports/Cucumber.json",
    "html:target/cucumber-reports/Cucumber.html"
  })
class ProductBehaviourRunner { }
