package com.camila.api.product.infrastructure.adapter.output;

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

import lombok.extern.slf4j.Slf4j;

/**
 * The type Random data generator.
 */
@Slf4j
public class RandomDataGenerator {
  private static final long NUM_ITEMS = 100_000L;

  private static final String DATA_GENERATED_JSON = "./camila-product-api/.operate/data/mongo/data-generated.script";

  private static final String LINE_TEMPLATE = """
      {"internalId":"%s", "name":"%s", "category":"%s", "salesUnits":%d, "stock":%s, "profitMargin":%.2f, "daysInStock":%d}
    """;

  private static final String[] CATEGORY_WORDS = new String[]{
    "SHIRT", "PANTS", "DRESS", "SWEATER", "JACKET", "COAT", "FOOTWEAR", "ACCESSORY"
  };

  private static final Map<String, String[]> CATEGORY_SPECIFIC_WORDS = Map.of(
    "SHIRT", new String[]{"BUTTON-UP", "POLO", "T-SHIRT", "HENLEY", "OXFORD", "FLANNEL",
      "LINEN", "SILK", "PLEATED", "CONTRASTING", "EMBROIDERED", "SLIM-FIT"},
    "PANTS", new String[]{"CHINO", "CARGO", "CORDUROY", "LINEN", "KHAKI", "PLEATED",
      "STRAIGHT", "SKINNY", "WIDE-LEG", "CROPPED", "SWEATPANTS", "LEGGINGS"},
    "DRESS", new String[]{"COCKTAIL", "MAXI", "MIDI", "MINI", "SHEATH", "WRAP", "SLIP",
      "FLORAL", "PATTERNED", "RUFFLED", "TIERED", "EMPIRE", "A-LINE"},
    "SWEATER", new String[]{"CABLE-KNIT", "CARDIGAN", "CASHMERE", "WOOL", "CREWNECK",
      "V-NECK", "TURTLENECK", "PULLOVER", "HOODIE", "SWEATSHIRT", "TANK", "CROPPED", "RIBBED"},
    "JACKET", new String[]{"BOMBER", "DENIM", "LEATHER", "QUILTED", "WINDBREAKER", "PUFFER",
      "UTILITY", "TRACK", "BLAZER", "PARKA", "TRENCH", "PEACOAT", "OVERSHIRT"},
    "COAT", new String[]{"TRENCH", "WOOL", "PEACOAT", "OVERCOAT", "DUFFLE", "PARKA",
      "RAINCOAT", "SHEARLING", "PUFFER", "TEDDY", "CARCOAT", "WRAP"},
    "FOOTWEAR", new String[]{"SNEAKER", "LOAFER", "DERBY", "OXFORD", "BOOT", "SANDAL",
      "SLIPPER", "ESPADRILLE", "MULE", "DRIVING SHOE"},
    "ACCESSORY", new String[]{"HAT", "SCARF", "GLOVES", "BELT", "TIE", "SOCKS", "WATCH",
      "SUNGLASSES", "BAG", "WALLET", "KEYCHAIN", "BRACELET", "NECKLACE", "EARRINGS", "RING"}
  );

  private static final String[] DESCRIPTIVE_WORDS = new String[]{
    "PREMIUM", "CLASSIC", "MODERN", "VINTAGE", "DELUXE", "SIGNATURE", "ESSENTIAL",
    "SLIM", "RELAXED", "TAILORED", "CASUAL", "ELEGANT", "LUXURY", "COMFORTABLE",
    "BASIC", "SPORTY", "BOHO", "ECO-FRIENDLY", "SUSTAINABLE", "ETHICAL", "CHIC",
    "FEMININE", "MASCULINE", "UNISEX", "EXCLUSIVE", "LIMITED EDITION", "HANDMADE",
    "ARTISANAL", "DESIGNER", "COLLABORATION", "STREETWEAR", "ATHLEISURE", "BOHEMIAN",
    "GLAMOROUS", "RETRO", "FESTIVAL", "RESORT", "URBAN", "COUNTRY", "BEACHWEAR",
    "BUSINESS", "FORMAL", "CASUAL", "LOUNGEWEAR", "ACTIVEWEAR", "OUTDOOR", "LOUNGE"
  };

  private static final String[] MATERIAL_WORDS = new String[]{
    "COTTON", "WOOL", "LINEN", "SILK", "CASHMERE", "DENIM", "POLYESTER",
    "LEATHER", "ORGANIC", "RECYCLED", "TWILL", "VELVET", "SATIN",
    "SUEDE", "FLEECE", "MOHAIR", "ALPACA", "VISCOSE", "LYOCELL",
    "ACRYLIC", "BAMBOO", "HERRINGBONE", "JACQUARD", "LACE", "CHIFFON",
    "SEERSUCKER", "TWEED", "CORDUROY", "BROCADE", "GABARDINE", "JERSEY",
    "CHAMBRAY", "FLANNEL", "TARTAN", "TULLE", "TERRYCLOTH", "SATEEN",
    "BURLAP", "SISAL", "RATTAN", "JUTE", "WOVEN", "KNITTED", "EMBROIDERED",
    "PRINTED", "DYE-TREATED", "CRINKLED", "RUCHED", "RUFFLED", "FRINGED"
  };

  private static final SecureRandom RANDOM = new SecureRandom();

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
      log.trace(e.getMessage());
    }
  }

  private static void writeLine(final BufferedWriter writer, final String line) {
    try {
      writer.write(line);
    } catch (IOException e) {
      log.trace(e.getMessage());
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
      final String category = CATEGORY_WORDS[random.nextInt(CATEGORY_WORDS.length)];
      final String name = generateRandomName(internalId, category);
      final int salesUnits;
      final double profitMargin;

      // Different categories have different sales patterns
      if (category.equals("SHIRT") || category.equals("PANTS")) {
        salesUnits = random.nextInt(1_000, 5_000); // High-volume items
        profitMargin = 0.15 + (random.nextDouble() * 0.25); // 15-40%
      } else if (category.equals("DRESS") || category.equals("JACKET")) {
        salesUnits = random.nextInt(100, 1_000); // Medium-volume
        profitMargin = 0.25 + (random.nextDouble() * 0.35); // 25-60%
      } else {
        salesUnits = random.nextInt(50, 2_000); // Variable volume
        profitMargin = 0.20 + (random.nextDouble() * 0.30); // 20-50%
      }

      final int daysInStock = random.nextInt(365);

      return LINE_TEMPLATE.formatted(internalId, name, category, salesUnits,
        stockToJson(random, category), profitMargin, daysInStock);
    });
  }

  private static String generateRandomName(final Integer internalId, final String category) {
    final String[] categoryWords = CATEGORY_SPECIFIC_WORDS.get(category);
    final String descriptiveWord = DESCRIPTIVE_WORDS[RANDOM.nextInt(DESCRIPTIVE_WORDS.length)];
    final String materialWord = RANDOM.nextBoolean() ? MATERIAL_WORDS[RANDOM.nextInt(MATERIAL_WORDS.length)] + " " : "";
    final String categoryWord = categoryWords[RANDOM.nextInt(categoryWords.length)];

    return "%s %s%s %s %07d".formatted(descriptiveWord, materialWord, categoryWord, category, internalId);
  }

  private static String stockToJson(final Random random, final String category) {
    final Map<String, Integer> stock;

    if (category.equals("FOOTWEAR")) {
      stock = Map.of(
        "6", random.nextInt(100),
        "7", random.nextInt(200),
        "8", random.nextInt(300),
        "9", random.nextInt(300),
        "10", random.nextInt(200),
        "11", random.nextInt(100)
      );
    } else if (category.equals("ACCESSORY")) {
      stock = Map.of("ONE_SIZE", random.nextInt(1_000));
    } else {
      stock = Map.of(
        "XS", random.nextInt(300),
        "S", random.nextInt(500),
        "M", random.nextInt(800),
        "L", random.nextInt(500),
        "XL", random.nextInt(300)
      );
    }

    return "{" + stock.entrySet().stream()
      .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
      .collect(Collectors.joining(",")) + "}";
  }
}
