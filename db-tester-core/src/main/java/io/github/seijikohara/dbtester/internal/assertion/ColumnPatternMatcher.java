package io.github.seijikohara.dbtester.internal.assertion;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Matches column names against glob patterns for column exclusion.
 *
 * <p>This utility supports glob-style patterns in column exclusion lists. Entries containing {@code
 * *} or {@code ?} wildcards are treated as patterns and matched against actual column names.
 * Entries without wildcards are treated as exact column names.
 *
 * <p>Pattern syntax:
 *
 * <ul>
 *   <li>{@code *} matches any sequence of characters (including empty)
 *   <li>{@code ?} matches any single character
 * </ul>
 *
 * <p>All matching is case-insensitive, consistent with existing column exclusion behavior.
 *
 * <p>Example patterns:
 *
 * <ul>
 *   <li>{@code *_AT} matches {@code CREATED_AT}, {@code UPDATED_AT}, {@code DELETED_AT}
 *   <li>{@code *_BY} matches {@code CREATED_BY}, {@code MODIFIED_BY}
 *   <li>{@code VERSION?} matches {@code VERSION1}, {@code VERSIONS}
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.annotation.DataSetSource#excludeColumns()
 */
public final class ColumnPatternMatcher {

  /** Private constructor to prevent instantiation. */
  private ColumnPatternMatcher() {}

  /**
   * Checks whether the given string contains glob wildcard characters.
   *
   * @param value the string to check
   * @return true if the string contains {@code *} or {@code ?}, false otherwise
   */
  public static boolean isPattern(final String value) {
    return value.contains("*") || value.contains("?");
  }

  /**
   * Resolves glob patterns in a set of exclude column entries against actual column names.
   *
   * <p>Entries without wildcards are returned unchanged. Entries containing {@code *} or {@code ?}
   * are matched against the provided column names, and matching column names replace the pattern
   * entry.
   *
   * @param excludeEntries the exclude column entries (may contain both exact names and patterns)
   * @param columnNames the actual column names to match patterns against
   * @return set of resolved column names (uppercase, no patterns remaining)
   */
  public static Set<String> resolvePatterns(
      final Collection<String> excludeEntries, final Collection<String> columnNames) {
    if (excludeEntries.isEmpty()) {
      return Set.of();
    }

    final var upperColumnNames =
        columnNames.stream()
            .map(name -> name.toUpperCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());

    return excludeEntries.stream()
        .flatMap(
            entry -> {
              if (isPattern(entry)) {
                final var pattern = globToRegex(entry.toUpperCase(Locale.ROOT));
                return upperColumnNames.stream().filter(name -> pattern.matcher(name).matches());
              } else {
                return java.util.stream.Stream.of(entry.toUpperCase(Locale.ROOT));
              }
            })
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Converts a glob pattern to a compiled regex Pattern.
   *
   * <p>The conversion escapes all regex special characters except {@code *} and {@code ?}, which
   * are converted to {@code .*} and {@code .} respectively.
   *
   * @param glob the glob pattern
   * @return compiled regex Pattern for case-insensitive matching
   */
  private static Pattern globToRegex(final String glob) {
    final var regex = new StringBuilder();
    for (int i = 0; i < glob.length(); i++) {
      final var c = glob.charAt(i);
      switch (c) {
        case '*' -> regex.append(".*");
        case '?' -> regex.append('.');
        default -> {
          if (isRegexSpecialChar(c)) {
            regex.append('\\');
          }
          regex.append(c);
        }
      }
    }
    return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
  }

  /**
   * Checks whether a character is a regex special character that needs escaping.
   *
   * @param c the character to check
   * @return true if the character is a regex special character
   */
  private static boolean isRegexSpecialChar(final char c) {
    return ".+^${}[]|()\\\t".indexOf(c) >= 0;
  }
}
