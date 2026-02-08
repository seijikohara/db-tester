package io.github.seijikohara.dbtester.internal.template;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes template expressions in CSV cell values.
 *
 * <p>This class resolves {@code ${...}} expressions in string values. Supported expression types:
 *
 * <ul>
 *   <li>{@code ${sequence:N}} — sequence number starting from N
 *   <li>{@code ${sequence}} — next sequence number (auto-increments)
 *   <li>{@code ${uuid}} — random UUID
 *   <li>{@code ${now}} — current date/time in ISO-8601 format
 *   <li>{@code ${now+Xd}}, {@code ${now-Xd}} — relative date/time (d=days, h=hours, m=minutes,
 *       s=seconds)
 *   <li>{@code ${faker.xxx.yyy}} — Datafaker expression (requires Datafaker on classpath)
 * </ul>
 *
 * <p>Instances are stateful due to sequence counters and should be created per table parse.
 * Unrecognized expressions are returned unchanged.
 *
 * @see <a href="https://www.datafaker.net/">Datafaker</a>
 */
public final class TemplateProcessor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TemplateProcessor.class);

  /** Pattern to detect {@code ${...}} expressions. */
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  /** Pattern to parse relative date/time expressions like {@code now-7d} or {@code now+1h}. */
  private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile("now([+-])(\\d+)([dhms])");

  /** ISO-8601 date/time formatter without offset. */
  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /** Sequence counter for {@code ${sequence}} expressions. */
  private final AtomicInteger sequenceCounter = new AtomicInteger(0);

  /** Datafaker instance loaded via reflection, or null if unavailable. */
  private final @Nullable Object faker;

  /** Creates a new processor and attempts to load Datafaker via reflection. */
  public TemplateProcessor() {
    this.faker = loadFaker();
  }

  /**
   * Checks whether the given value contains template expressions.
   *
   * @param value the value to check
   * @return true if the value contains at least one {@code ${...}} expression
   */
  public static boolean containsExpression(final String value) {
    return EXPRESSION_PATTERN.matcher(value).find();
  }

  /**
   * Processes all template expressions in the given value.
   *
   * <p>If the value contains no expressions, it is returned unchanged. Each {@code ${...}}
   * expression is resolved independently. Unrecognized expressions are returned as-is.
   *
   * @param value the value potentially containing template expressions
   * @return the value with all recognized expressions resolved
   */
  public String process(final String value) {
    final var matcher = EXPRESSION_PATTERN.matcher(value);
    if (!matcher.find()) {
      return value;
    }

    matcher.reset();
    final var result = new StringBuilder();
    while (matcher.find()) {
      final var expression = matcher.group(1);
      final var resolved = resolveExpression(expression);
      matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Resolves a single expression (without the {@code ${}} wrapper).
   *
   * @param expression the expression to resolve
   * @return the resolved value
   */
  private String resolveExpression(final String expression) {
    if (expression.startsWith("sequence")) {
      return resolveSequence(expression);
    }
    if ("uuid".equals(expression)) {
      return UUID.randomUUID().toString();
    }
    if ("now".equals(expression)) {
      return LocalDateTime.now(ZoneId.systemDefault()).format(ISO_FORMATTER);
    }
    if (expression.startsWith("now")) {
      return resolveRelativeDateTime(expression);
    }
    if (expression.startsWith("faker.")) {
      return resolveFaker(expression);
    }
    logger.debug("Unrecognized template expression: {}", expression);
    return String.format("${%s}", expression);
  }

  /**
   * Resolves a sequence expression.
   *
   * <p>{@code ${sequence:N}} sets the counter to N and returns N. {@code ${sequence}} increments
   * the counter and returns the next value.
   *
   * @param expression the sequence expression
   * @return the resolved sequence number as a string
   */
  private String resolveSequence(final String expression) {
    if (expression.contains(":")) {
      final var parts = expression.split(":", 2);
      final var startValue = Integer.parseInt(parts[1].trim());
      sequenceCounter.set(startValue);
      return String.valueOf(startValue);
    }
    return String.valueOf(sequenceCounter.incrementAndGet());
  }

  /**
   * Resolves a relative date/time expression.
   *
   * <p>Supports formats like {@code now-7d}, {@code now+1h}, {@code now-30m}, {@code now+10s}.
   *
   * @param expression the relative date/time expression
   * @return the resolved date/time as an ISO-8601 string
   */
  private String resolveRelativeDateTime(final String expression) {
    final var matcher = RELATIVE_TIME_PATTERN.matcher(expression);
    if (!matcher.matches()) {
      logger.debug("Invalid relative date/time expression: {}", expression);
      return String.format("${%s}", expression);
    }

    final var sign = matcher.group(1);
    final var amount = Long.parseLong(matcher.group(2));
    final var unit = matcher.group(3);
    final var signedAmount = "+".equals(sign) ? amount : -amount;

    final var now = LocalDateTime.now(ZoneId.systemDefault());
    final var resolved =
        switch (unit) {
          case "d" -> now.plusDays(signedAmount);
          case "h" -> now.plusHours(signedAmount);
          case "m" -> now.plusMinutes(signedAmount);
          case "s" -> now.plusSeconds(signedAmount);
          default -> now;
        };
    return resolved.format(ISO_FORMATTER);
  }

  /**
   * Resolves a Datafaker expression via reflection.
   *
   * <p>Expressions like {@code faker.name.fullName} are resolved by chaining method calls on the
   * Faker instance: {@code faker.name().fullName()}. If Datafaker is not on the classpath or
   * resolution fails, the original expression is returned unchanged.
   *
   * @param expression the Faker expression (e.g., {@code faker.name.fullName})
   * @return the generated value, or the original expression if resolution fails
   */
  private String resolveFaker(final String expression) {
    if (faker == null) {
      logger.debug(
          "Datafaker not available on classpath; returning expression unchanged: {}", expression);
      return String.format("${%s}", expression);
    }

    final var path = expression.substring("faker.".length());
    final var parts = path.split("\\.", -1);

    try {
      Object current = faker;
      for (final var part : parts) {
        final var method = findMethod(current.getClass(), part);
        if (method == null) {
          logger.debug("Faker method not found: {} on {}", part, current.getClass().getName());
          return String.format("${%s}", expression);
        }
        current = method.invoke(current);
        if (current == null) {
          return String.format("${%s}", expression);
        }
      }
      return String.valueOf(current);
    } catch (final ReflectiveOperationException e) {
      logger.debug("Failed to resolve Faker expression: {}", expression, e);
      return String.format("${%s}", expression);
    }
  }

  /**
   * Finds a no-argument method by name on the given class.
   *
   * @param clazz the class to search
   * @param methodName the method name
   * @return the method, or null if not found
   */
  private static @Nullable Method findMethod(final Class<?> clazz, final String methodName) {
    try {
      return clazz.getMethod(methodName);
    } catch (final NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Attempts to load the Datafaker {@code Faker} class via reflection.
   *
   * @return a Faker instance, or null if Datafaker is not on the classpath
   */
  private static @Nullable Object loadFaker() {
    try {
      final var fakerClass = Class.forName("net.datafaker.Faker");
      return fakerClass.getDeclaredConstructor().newInstance();
    } catch (final ReflectiveOperationException e) {
      logger.debug("Datafaker not found on classpath; faker expressions will not be resolved");
      return null;
    }
  }
}
