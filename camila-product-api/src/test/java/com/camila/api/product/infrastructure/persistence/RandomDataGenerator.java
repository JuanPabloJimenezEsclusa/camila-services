package com.camila.api.product.infrastructure.persistence;

import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Random data generator.
 */
public class RandomDataGenerator {
  private static final String DATA_TEMPLATE = "{\"internalId\":\"%s\", \"name\":\"%s\", \"category\":\"%s\", \"salesUnits\":%d, \"stock\":%s}";
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
    int numItems = 5_000;
    var random = new Random();

    String collect = Stream.iterate(7, n -> n+1).limit(numItems).map(internalId -> {
      String name = generateRandomName(internalId);
      String category = CATEGORY_WORDS[0];
      int salesUnits = random.nextInt(200);
      return String.format(DATA_TEMPLATE, internalId, name, category, salesUnits, stockToJson(random));
    }).collect(Collectors.joining(",\n"));
    System.out.println(collect);
  }

  private static String generateRandomName(int id) {
    var random = new Random();
    String word1 = NAME_WORDS[random.nextInt(NAME_WORDS.length)];
    String word2 = NAME_WORDS[random.nextInt(NAME_WORDS.length)];
    return String.format("%s %s %05d%s SHIRT", word1, word2, 0, id);
  }

  private static String stockToJson(Random random) {
    Map<String, Integer> stock = Map.of(
      "S", random.nextInt(100),
      "M", random.nextInt(100),
      "L", random.nextInt(50));

    return  "{" + stock.entrySet().stream().map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
      .collect(Collectors.joining(",")) + "}";
  }
}
