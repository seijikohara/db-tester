package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TableName}. */
@DisplayName("TableName")
class TableNameTest {

  /** Tests for the TableName class. */
  TableNameTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates instance with valid name. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when valid name provided")
    void should_create_instance_when_valid_name_provided() {
      // Given
      final var name = "USERS";

      // When
      final var tableName = new TableName(name);

      // Then
      assertEquals(name, tableName.value(), "value should match");
    }

    /** Verifies that constructor preserves case. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve case")
    void should_preserve_case() {
      // Given
      final var mixedCase = "UserAccounts";

      // When
      final var tableName = new TableName(mixedCase);

      // Then
      assertEquals(mixedCase, tableName.value(), "should preserve case");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from name")
    void should_trim_whitespace_from_name() {
      // Given
      final var nameWithSpaces = "  ORDERS  ";

      // When
      final var tableName = new TableName(nameWithSpaces);

      // Then
      assertEquals("ORDERS", tableName.value(), "should trim whitespace");
    }

    /** Verifies that constructor throws exception for blank name. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when name is blank")
    void should_throw_exception_when_name_is_blank() {
      // Given
      final var blankName = "   ";

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new TableName(blankName),
              "should throw IllegalArgumentException");
      assertTrue(
          Objects.requireNonNull(exception.getMessage()).contains("must not be blank"),
          "message should indicate blank not allowed");
    }

    /** Verifies that constructor throws exception for empty name. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when name is empty")
    void should_throw_exception_when_name_is_empty() {
      // Given
      final var emptyName = "";

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new TableName(emptyName),
          "should throw IllegalArgumentException for empty name");
    }

    /** Verifies that constructor accepts special characters in table names. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept special characters in table names")
    void should_accept_special_characters_in_table_names() {
      // Given
      final var nameWithSpecialChars = "USER_ACCOUNTS_V2";

      // When
      final var tableName = new TableName(nameWithSpecialChars);

      // Then
      assertEquals(nameWithSpecialChars, tableName.value(), "should accept special characters");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(TableName) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var orders = new TableName("ORDERS");
      final var ordersEqual = new TableName("ORDERS");
      final var products = new TableName("PRODUCTS");
      final var users = new TableName("USERS");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(orders.compareTo(products) < 0, "ORDERS should be before PRODUCTS"),
          () -> assertTrue(products.compareTo(users) < 0, "PRODUCTS should be before USERS"),
          () -> assertTrue(users.compareTo(orders) > 0, "USERS should be after ORDERS"),
          () -> assertEquals(0, orders.compareTo(ordersEqual), "ORDERS should equal ORDERS"));
    }

    /** Verifies that compareTo is case-sensitive. */
    @Test
    @Tag("edge-case")
    @DisplayName("should be case-sensitive when comparing")
    void should_be_case_sensitive_when_comparing() {
      // Given
      final var uppercase = new TableName("USERS");
      final var lowercase = new TableName("users");

      // When & Then
      assertNotEquals(0, uppercase.compareTo(lowercase), "case-sensitive comparison");
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /** Verifies that instances with same value are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when values are the same")
    void should_be_equal_when_values_are_the_same() {
      // Given
      final var name1 = new TableName("USERS");
      final var name2 = new TableName("USERS");

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(name1, name2, "should be equal"),
          () -> assertEquals(name1.hashCode(), name2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that instances with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void should_not_be_equal_when_values_differ() {
      // Given
      final var name1 = new TableName("USERS");
      final var name2 = new TableName("ORDERS");

      // When & Then
      assertNotEquals(name1, name2, "should not be equal");
    }

    /** Verifies that instances with different case are not equal. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not be equal when case differs")
    void should_not_be_equal_when_case_differs() {
      // Given
      final var uppercase = new TableName("USERS");
      final var lowercase = new TableName("users");

      // When & Then
      assertNotEquals(uppercase, lowercase, "case-sensitive equality");
    }
  }

  /** Tests for the toString method. */
  @Nested
  @DisplayName("toString() method")
  class ToStringMethod {

    /** Tests for the toString method. */
    ToStringMethod() {}

    /** Verifies that toString contains value. */
    @Test
    @Tag("normal")
    @DisplayName("should contain value in string representation")
    void should_contain_value_in_string_representation() {
      // Given
      final var tableName = new TableName("PRODUCTS");

      // When
      final var result = tableName.toString();

      // Then
      assertTrue(result.contains("PRODUCTS"), "should contain the value");
    }
  }
}
