package io.github.seijikohara.dbtester.internal.jdbc;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.get;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An iterator adapter for JDBC {@link ResultSet} that enables Stream-based processing.
 *
 * <p>This class wraps a {@link ResultSet} and provides both {@link Iterator} and {@link Stream}
 * interfaces for functional-style processing of database query results. The iterator is designed
 * for single-pass, forward-only traversal consistent with ResultSet semantics.
 *
 * <p>Example usage with Stream:
 *
 * <pre>{@code
 * try (ResultSet resultSet = statement.executeQuery()) {
 *     List<String> names = ResultSetIterator.stream(resultSet, row -> row.getString("name"))
 *         .filter(Objects::nonNull)
 *         .collect(Collectors.toList());
 * }
 * }</pre>
 *
 * <p><strong>Important:</strong> The Stream returned by {@link #stream(ResultSet, RowMapper)} does
 * not close the underlying ResultSet. The caller is responsible for closing the ResultSet,
 * typically using try-with-resources.
 *
 * <p>This class is not thread-safe as ResultSet itself is not thread-safe.
 *
 * @param <T> the type of elements returned by this iterator
 * @see ResultSet
 * @see Stream
 */
public final class ResultSetIterator<T> implements Iterator<T> {

  /** The underlying JDBC ResultSet. */
  private final ResultSet resultSet;

  /** The mapper function that converts each row to the target type. */
  private final Function<ResultSet, T> rowMapper;

  /** Indicates whether we have checked if the next row exists. */
  private boolean hasNextChecked;

  /** Cached result of hasNext() check. */
  private boolean hasNextResult;

  /**
   * Creates a new ResultSetIterator.
   *
   * @param resultSet the ResultSet to iterate over
   * @param rowMapper the function to map each row to the target type
   */
  private ResultSetIterator(final ResultSet resultSet, final Function<ResultSet, T> rowMapper) {
    this.resultSet = resultSet;
    this.rowMapper = rowMapper;
    this.hasNextChecked = false;
    this.hasNextResult = false;
  }

  /**
   * Creates a Stream from a ResultSet using the provided row mapper.
   *
   * <p>The returned Stream is sequential and ordered. It does not close the underlying ResultSet;
   * the caller must manage the ResultSet lifecycle.
   *
   * @param <T> the type of elements in the resulting Stream
   * @param resultSet the ResultSet to stream over
   * @param rowMapper the function to map each row to the target type
   * @return a Stream of mapped elements
   * @throws IllegalArgumentException if resultSet or rowMapper is null
   */
  public static <T> Stream<T> stream(
      final ResultSet resultSet, final Function<ResultSet, T> rowMapper) {
    final var iterator = new ResultSetIterator<>(resultSet, rowMapper);
    final var spliterator =
        Spliterators.spliteratorUnknownSize(
            iterator, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
    return StreamSupport.stream(spliterator, false);
  }

  @Override
  public boolean hasNext() {
    if (hasNextChecked) {
      return hasNextResult;
    }

    hasNextResult = get(resultSet::next);
    hasNextChecked = true;
    return hasNextResult;
  }

  @Override
  public T next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more rows in ResultSet");
    }

    hasNextChecked = false;
    return rowMapper.apply(resultSet);
  }
}
