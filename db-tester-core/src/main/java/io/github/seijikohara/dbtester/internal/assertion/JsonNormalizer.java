package io.github.seijikohara.dbtester.internal.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Normalizes JSON strings for structural comparison.
 *
 * <p>This class parses JSON strings and re-serializes them with sorted object keys and consistent
 * formatting, enabling structural equivalence comparison that ignores key order and whitespace
 * differences.
 *
 * <p>This implementation uses a minimal recursive descent parser without external JSON library
 * dependencies. It handles standard JSON types: objects, arrays, strings, numbers, booleans, and
 * null.
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>Maximum nesting depth of {@value #MAX_NESTING_DEPTH} levels
 *   <li>Maximum input length of {@value #MAX_INPUT_LENGTH} characters
 *   <li>Unicode surrogate pairs in JSON unicode escapes are handled correctly
 * </ul>
 */
final class JsonNormalizer {

  /** Maximum allowed nesting depth for JSON structures. */
  static final int MAX_NESTING_DEPTH = 128;

  /** Maximum allowed input length in characters (1 MB of UTF-16). */
  static final int MAX_INPUT_LENGTH = 1_048_576;

  /** The JSON input string. */
  private final String input;

  /** Current position in the input string. */
  private int pos;

  /** Current nesting depth for recursion protection. */
  private int depth;

  /**
   * Creates a new JSON normalizer for the specified input.
   *
   * @param input the JSON string to normalize
   */
  private JsonNormalizer(final String input) {
    this.input = input;
    this.pos = 0;
    this.depth = 0;
  }

  /**
   * Normalizes a JSON string by sorting object keys and removing insignificant whitespace.
   *
   * <p>Returns the original string unchanged if the input exceeds {@value #MAX_INPUT_LENGTH}
   * characters or if parsing fails.
   *
   * @param json the JSON string to normalize
   * @return the normalized JSON string, or the original string if parsing fails
   */
  static String normalize(final String json) {
    final var trimmed = json.trim();
    if (trimmed.length() > MAX_INPUT_LENGTH) {
      return json;
    }
    try {
      final var normalizer = new JsonNormalizer(trimmed);
      final var value = normalizer.parseValue();
      normalizer.skipWhitespace();
      if (normalizer.pos != normalizer.input.length()) {
        return json;
      }
      return serialize(value);
    } catch (final JsonParseException e) {
      return json;
    }
  }

  /**
   * Checks if a string looks like JSON (starts with '{' or '[').
   *
   * @param value the string to check
   * @return true if the string appears to be JSON
   */
  static boolean looksLikeJson(final String value) {
    final var trimmed = value.trim();
    return (trimmed.startsWith("{") && trimmed.endsWith("}"))
        || (trimmed.startsWith("[") && trimmed.endsWith("]"));
  }

  /**
   * Parses a JSON value.
   *
   * <p>JSON string values are wrapped in {@link JsonString} to distinguish them from literal values
   * (numbers, booleans, null) during serialization.
   *
   * @return the parsed value (Map, List, JsonString, or String for literals)
   * @throws JsonParseException if nesting depth exceeds {@value #MAX_NESTING_DEPTH}
   */
  private Object parseValue() {
    skipWhitespace();
    if (pos >= input.length()) {
      throw new JsonParseException("Unexpected end of input");
    }
    final var ch = input.charAt(pos);
    return switch (ch) {
      case '{' -> {
        checkDepth();
        yield parseObject();
      }
      case '[' -> {
        checkDepth();
        yield parseArray();
      }
      case '"' -> new JsonString(parseString());
      case 't', 'f' -> parseBoolean();
      case 'n' -> parseNull();
      default -> {
        if (ch == '-' || (ch >= '0' && ch <= '9')) {
          yield parseNumber();
        }
        throw new JsonParseException(
            String.format("Unexpected character: %c at position %d", ch, pos));
      }
    };
  }

  /**
   * Checks and increments the nesting depth.
   *
   * @throws JsonParseException if the maximum nesting depth is exceeded
   */
  private void checkDepth() {
    depth++;
    if (depth > MAX_NESTING_DEPTH) {
      throw new JsonParseException(
          String.format("Maximum nesting depth of %d exceeded", MAX_NESTING_DEPTH));
    }
  }

  /**
   * Parses a JSON object.
   *
   * @return a TreeMap with sorted keys
   */
  private Map<String, Object> parseObject() {
    final var map = new TreeMap<String, Object>();
    pos++; // skip '{'
    skipWhitespace();
    if (pos < input.length() && input.charAt(pos) == '}') {
      pos++;
      depth--;
      return map;
    }
    while (pos < input.length()) {
      skipWhitespace();
      final var key = parseString();
      skipWhitespace();
      expect(':');
      final var value = parseValue();
      map.put(key, value);
      skipWhitespace();
      if (pos < input.length() && input.charAt(pos) == ',') {
        pos++;
      } else {
        break;
      }
    }
    expect('}');
    depth--;
    return map;
  }

  /**
   * Parses a JSON array.
   *
   * @return a List of parsed values
   */
  private List<Object> parseArray() {
    final var list = new ArrayList<Object>();
    pos++; // skip '['
    skipWhitespace();
    if (pos < input.length() && input.charAt(pos) == ']') {
      pos++;
      depth--;
      return list;
    }
    while (pos < input.length()) {
      list.add(parseValue());
      skipWhitespace();
      if (pos < input.length() && input.charAt(pos) == ',') {
        pos++;
      } else {
        break;
      }
    }
    expect(']');
    depth--;
    return list;
  }

  /**
   * Parses a JSON string.
   *
   * <p>Handles all JSON escape sequences including Unicode escapes with surrogate pairs.
   *
   * @return the parsed string value
   */
  private String parseString() {
    expect('"');
    final var sb = new StringBuilder();
    while (pos < input.length()) {
      final var ch = input.charAt(pos);
      if (ch == '"') {
        pos++;
        return sb.toString();
      }
      if (ch == '\\') {
        pos++;
        if (pos >= input.length()) {
          throw new JsonParseException("Unexpected end of input in string escape");
        }
        final var escaped = input.charAt(pos);
        switch (escaped) {
          case '"', '\\', '/' -> sb.append(escaped);
          case 'b' -> sb.append('\b');
          case 'f' -> sb.append('\f');
          case 'n' -> sb.append('\n');
          case 'r' -> sb.append('\r');
          case 't' -> sb.append('\t');
          case 'u' -> {
            final var codeUnit = parseUnicodeEscape();
            if (Character.isHighSurrogate(codeUnit)) {
              // Expect low surrogate pair
              if (pos + 1 < input.length()
                  && input.charAt(pos + 1) == '\\'
                  && pos + 2 < input.length()
                  && input.charAt(pos + 2) == 'u') {
                pos += 2; // skip backslash-u prefix
                final var lowSurrogate = parseUnicodeEscape();
                if (Character.isLowSurrogate(lowSurrogate)) {
                  sb.appendCodePoint(Character.toCodePoint(codeUnit, lowSurrogate));
                } else {
                  sb.append(codeUnit);
                  sb.append(lowSurrogate);
                }
              } else {
                sb.append(codeUnit);
              }
            } else {
              sb.append(codeUnit);
            }
          }
          default ->
              throw new JsonParseException(String.format("Invalid escape character: %c", escaped));
        }
      } else {
        sb.append(ch);
      }
      pos++;
    }
    throw new JsonParseException("Unterminated string");
  }

  /**
   * Parses a four-digit Unicode escape sequence.
   *
   * <p>Assumes the parser is positioned after the 'u' character. Advances the position by 4
   * characters.
   *
   * @return the parsed Unicode code unit
   * @throws JsonParseException if the escape sequence is invalid
   */
  private char parseUnicodeEscape() {
    if (pos + 4 >= input.length()) {
      throw new JsonParseException("Invalid unicode escape");
    }
    final var hex = input.substring(pos + 1, pos + 5);
    pos += 4;
    try {
      return (char) Integer.parseInt(hex, 16);
    } catch (final NumberFormatException e) {
      throw new JsonParseException(String.format("Invalid unicode escape: \\u%s", hex));
    }
  }

  /**
   * Parses a JSON number.
   *
   * @return the number as a string (preserving exact representation)
   */
  private String parseNumber() {
    final var start = pos;
    if (pos < input.length() && input.charAt(pos) == '-') {
      pos++;
    }
    while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
      pos++;
    }
    if (pos < input.length() && input.charAt(pos) == '.') {
      pos++;
      while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
        pos++;
      }
    }
    if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
      pos++;
      if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
        pos++;
      }
      while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
        pos++;
      }
    }
    return input.substring(start, pos);
  }

  /**
   * Parses a JSON boolean value.
   *
   * @return "true" or "false" as a string
   */
  private String parseBoolean() {
    if (input.startsWith("true", pos)) {
      pos += 4;
      return "true";
    }
    if (input.startsWith("false", pos)) {
      pos += 5;
      return "false";
    }
    throw new JsonParseException(String.format("Unexpected token at position %d", pos));
  }

  /**
   * Parses a JSON null value.
   *
   * @return the string "null"
   */
  private String parseNull() {
    if (input.startsWith("null", pos)) {
      pos += 4;
      return "null";
    }
    throw new JsonParseException(String.format("Unexpected token at position %d", pos));
  }

  /** Skips whitespace characters in the input. */
  private void skipWhitespace() {
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }
  }

  /**
   * Expects and consumes the specified character.
   *
   * @param expected the expected character
   */
  private void expect(final char expected) {
    if (pos >= input.length() || input.charAt(pos) != expected) {
      throw new JsonParseException(String.format("Expected '%c' at position %d", expected, pos));
    }
    pos++;
  }

  /**
   * Serializes a parsed JSON value to a normalized string with sorted object keys.
   *
   * <p>{@link JsonString} instances are serialized as quoted strings. Plain {@link String} values
   * represent JSON literals (numbers, booleans, null) and are serialized without quotes.
   *
   * @param value the parsed value
   * @return the serialized JSON string
   */
  @SuppressWarnings("unchecked")
  private static String serialize(final Object value) {
    return switch (value) {
      case Map<?, ?> map -> serializeObject((Map<String, Object>) map);
      case List<?> list -> serializeArray(list);
      case JsonString jsonStr -> escapeString(jsonStr.value());
      case String str -> str;
      default -> value.toString();
    };
  }

  /**
   * Serializes a JSON object with sorted keys.
   *
   * @param map the object to serialize
   * @return the serialized JSON object string
   */
  private static String serializeObject(final Map<String, Object> map) {
    final var sb = new StringBuilder("{");
    var first = true;
    for (final var entry : map.entrySet()) {
      if (!first) {
        sb.append(",");
      }
      first = false;
      sb.append(escapeString(entry.getKey()));
      sb.append(":");
      sb.append(serialize(entry.getValue()));
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Serializes a JSON array.
   *
   * @param list the array to serialize
   * @return the serialized JSON array string
   */
  private static String serializeArray(final List<?> list) {
    final var sb = new StringBuilder("[");
    var first = true;
    for (final var item : list) {
      if (!first) {
        sb.append(",");
      }
      first = false;
      sb.append(serialize(item));
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Escapes a string value for JSON output.
   *
   * @param str the string to escape
   * @return the escaped JSON string with surrounding quotes
   */
  private static String escapeString(final String str) {
    final var sb = new StringBuilder("\"");
    for (var i = 0; i < str.length(); i++) {
      final var ch = str.charAt(i);
      switch (ch) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (ch < 0x20) {
            sb.append(String.format("\\u%04x", (int) ch));
          } else {
            sb.append(ch);
          }
        }
      }
    }
    sb.append("\"");
    return sb.toString();
  }

  /**
   * Wrapper for JSON string values to distinguish them from JSON literals during serialization.
   *
   * <p>JSON string values (parsed from quoted strings) are wrapped in this record, while literal
   * values (numbers, booleans, null) remain as plain {@link String} instances.
   *
   * @param value the string value
   */
  private record JsonString(String value) {}

  /** Exception thrown when JSON parsing fails. */
  private static final class JsonParseException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new JSON parse exception.
     *
     * @param message the error message
     */
    JsonParseException(final String message) {
      super(message);
    }
  }
}
