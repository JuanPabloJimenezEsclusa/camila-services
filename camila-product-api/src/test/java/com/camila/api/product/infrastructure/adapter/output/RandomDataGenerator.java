package com.camila.api.product.infrastructure.adapter.output;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Random data generator.
 */
@Slf4j
public class RandomDataGenerator {
  private static final long NUM_ITEMS = 100_000L;

  private static final String DATA_GENERATED_JSON = "./camila-product-api/.operate/data/couchbase/sample-data.script";

  private static final String LINE_TEMPLATE = """
    {"internalId":"%s", "name":"%s", "category":"%s", "salesUnits":%d, "stock":%s}
  """;

  private static final String[] CATEGORY_WORDS = new String[]{"SHIRT"};

  private static final String[] NAME_WORDS = new String[]{"EXQUISITE", "SPLENDID", "WONDERFUL", "MARVELOUS", "MAGNIFICENT",
    "GLAMOROUS", "ELEGANT", "CLASSY", "DAZZLING", "BRILLIANT", "FABULOUS", "IMPRESSIVE", "CHIC", "STYLISH", "TRENDY",
    "UNIQUE", "STUNNING", "ELEGANT", "POSH", "GRACEFUL", "LAVISH", "RADIANT", "GLAMOUR", "UPSCALE", "DELUXE", "EXCEPTIONAL",
    "EYE-CATCHING", "SOPHISTICATED", "GRAND", "ROYAL", "OPULENT", "REGAL", "STATELY", "MAGNANIMOUS", "PROMINENT", "PRESTIGIOUS",
    "DIGNIFIED", "GRANDIOSE", "AWE-INSPIRING", "LUXURIOUS", "PRISTINE", "MAJESTIC", "HIGH-CLASS", "ORNATE", "PAMPERED",
    "IMMACULATE", "EUPHORIC", "GLITTERING"};

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    final var random = new SecureRandom();

    try (var writer = getWriter(); var reader = getReader(random)) {
      reader.forEach(line -> writeLine(writer, line));
      writer.flush();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  private static void writeLine(final BufferedWriter writer, final String line) {
    try {
      writer.write(line);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  private static BufferedWriter getWriter() throws IOException {
    return Files.newBufferedWriter(
      Path.of(DATA_GENERATED_JSON),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static Stream<String> getReader(final Random random) {
    return Stream.iterate(7, n -> n + 1).parallel().limit(NUM_ITEMS).map(internalId -> {
      final String name = generateRandomName(internalId);
      final String category = CATEGORY_WORDS[0];
      final int salesUnits = random.nextInt(5_000);
      return LINE_TEMPLATE.formatted(internalId, name, category, salesUnits, stockToJson(random));
    });
  }

  private static String generateRandomName(final int id) {
    final var random = new SecureRandom();
    final String word1 = NAME_WORDS[random.nextInt(NAME_WORDS.length)];
    final String word2 = NAME_WORDS[random.nextInt(NAME_WORDS.length)];
    return "%s %s %05d%s SHIRT".formatted(word1, word2, 0, id);
  }

  private static String stockToJson(final Random random) {
    final var stock = Map.of(
      "S", random.nextInt(1_000),
      "M", random.nextInt(1_000),
      "L", random.nextInt(5_000));

    return  "{" + stock.entrySet().stream().map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
      .collect(Collectors.joining(",")) + "}";
  }
}
