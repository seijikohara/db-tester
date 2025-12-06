package io.github.seijikohara.dbtester.junit.jupiter.extension;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.internal.context.TestContext;
import io.github.seijikohara.dbtester.junit.jupiter.lifecycle.ExpectationVerifier;
import io.github.seijikohara.dbtester.junit.jupiter.lifecycle.PreparationExecutor;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Jupiter extension for database testing.
 *
 * <p>This extension processes {@link Preparation} and {@link Expectation} annotations to set up
 * test data before each test and verify database state after each test.
 *
 * <p>The extension performs three responsibilities:
 *
 * <ol>
 *   <li>Manages per-class state (configuration, {@link DataSourceRegistry}, lifecycle coordinator)
 *       using the {@link ExtensionContext} store.
 *   <li>Before each test, resolves {@link Preparation} declarations and executes the resulting
 *       datasets.
 *   <li>After each test, resolves {@link Expectation} declarations and validates the database
 *       contents.
 * </ol>
 *
 * @see Preparation
 * @see Expectation
 */
public class DatabaseTestExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  /** Logger for tracking test execution progress and errors. */
  private static final Logger logger = LoggerFactory.getLogger(DatabaseTestExtension.class);

  /** Store key for the configuration instance. */
  private static final String STORE_KEY_CONFIGURATION = "configuration";

  /** Store key for the data source registry instance. */
  private static final String STORE_KEY_REGISTRY = "registry";

  /** Executor for handling test preparation phase. */
  private final PreparationExecutor preparationExecutor = new PreparationExecutor();

  /** Verifier for handling test expectation verification phase. */
  private final ExpectationVerifier expectationVerifier = new ExpectationVerifier();

  /**
   * Creates a database test extension with default configuration.
   *
   * <p>Configuration and {@link DataSourceRegistry} instances are created lazily per test class
   * using the JUnit {@link ExtensionContext} store mechanism. This ensures proper isolation between
   * test classes while enabling configuration sharing within a class hierarchy.
   */
  public DatabaseTestExtension() {
    // Configuration and registry are created lazily per test class via Store
  }

  /**
   * Gets or creates the DataSourceRegistry for the current test class.
   *
   * <p>The registry is stored in the root context's Store, making it available to all tests in the
   * class hierarchy. Tests should call this method in {@code @BeforeAll} to register data sources.
   *
   * @param extensionContext the extension context
   * @return the data source registry for this test class
   */
  public static DataSourceRegistry getRegistry(final ExtensionContext extensionContext) {
    return getOrCreate(
        extensionContext, STORE_KEY_REGISTRY, DataSourceRegistry.class, DataSourceRegistry::new);
  }

  /**
   * Stores a custom configuration for the current test class.
   *
   * <p>This method allows tests to customize the framework's behavior by providing a custom {@link
   * Configuration} instance. It must be called in {@code @BeforeAll} before any test execution to
   * ensure the configuration is available when needed.
   *
   * <p>If not called, the framework uses {@link Configuration#defaults()}.
   *
   * @param extensionContext the extension context
   * @param configuration the custom configuration to use for this test class
   */
  public static void setConfiguration(
      final ExtensionContext extensionContext, final Configuration configuration) {
    final var store = getClassScopedStore(extensionContext);
    store.put(STORE_KEY_CONFIGURATION, configuration);
  }

  /**
   * Called before each test method.
   *
   * <p>If the test method or class is annotated with {@link Preparation}, this callback executes
   * the preparation phase by loading and applying datasets to the database.
   *
   * @param context the current extension context
   */
  @Override
  public void beforeEach(final ExtensionContext context) {
    Optional.ofNullable(findPreparation(context))
        .ifPresent(
            preparation -> {
              final var testContext = createTestContext(context);
              logger.debug(
                  "Executing beforeEach for {}.{}()",
                  testContext.testClass().getSimpleName(),
                  testContext.testMethod().getName());
              preparationExecutor.execute(testContext, preparation);
            });
  }

  /**
   * Called after each test method.
   *
   * <p>If the test method or class is annotated with {@link Expectation}, this callback executes
   * the expectation phase by loading expected datasets and comparing them with the actual database
   * state.
   *
   * @param context the current extension context
   */
  @Override
  public void afterEach(final ExtensionContext context) {
    Optional.ofNullable(findExpectation(context))
        .ifPresent(
            expectation -> {
              final var testContext = createTestContext(context);
              logger.debug(
                  "Executing afterEach for {}.{}()",
                  testContext.testClass().getSimpleName(),
                  testContext.testMethod().getName());
              expectationVerifier.verify(testContext, expectation);
            });
  }

  /**
   * Determines if this extension can resolve the specified parameter.
   *
   * <p>This extension supports injecting {@link ExtensionContext} into test methods and lifecycle
   * methods (including {@code @BeforeAll} when using {@code @TestInstance(Lifecycle.PER_CLASS)}).
   *
   * @param parameterContext the context for the parameter
   * @param extensionContext the extension context
   * @return {@code true} if the parameter type is {@link ExtensionContext}
   */
  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == ExtensionContext.class;
  }

  /**
   * Resolves the parameter value.
   *
   * <p>Returns the {@link ExtensionContext} instance for the current test execution.
   *
   * @param parameterContext the context for the parameter
   * @param extensionContext the extension context
   * @return the extension context instance
   */
  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    return extensionContext;
  }

  /**
   * Creates a {@link TestContext} from the JUnit {@link ExtensionContext}.
   *
   * <p>This method extracts the test class, test method, configuration, and registry from the
   * extension context and assembles them into a TestContext instance for use by lifecycle
   * executors.
   *
   * @param context the JUnit extension context providing test metadata
   * @return the test context containing all necessary information for test execution
   */
  private TestContext createTestContext(final ExtensionContext context) {
    final var testClass = context.getRequiredTestClass();
    final var testMethod = context.getRequiredTestMethod();
    final var configuration = getConfiguration(context);
    final var registry = getRegistry(context);

    return new TestContext(testClass, testMethod, configuration, registry);
  }

  /**
   * Finds the effective {@link Preparation} annotation for the current test.
   *
   * <p>Method-level annotations take precedence over class-level annotations. This method searches
   * first at the test method level, then falls back to the test class level if not found at the
   * method level.
   *
   * @param context the extension context providing access to test metadata
   * @return the preparation annotation if found at method or class level, or null if not present
   */
  private Preparation findPreparation(final ExtensionContext context) {
    final var method = context.getRequiredTestMethod();
    return Optional.ofNullable(method.getAnnotation(Preparation.class))
        .orElseGet(() -> context.getRequiredTestClass().getAnnotation(Preparation.class));
  }

  /**
   * Finds the effective {@link Expectation} annotation for the current test.
   *
   * <p>Method-level annotations take precedence over class-level annotations. This method searches
   * first at the test method level, then falls back to the test class level if not found at the
   * method level.
   *
   * @param context the extension context providing access to test metadata
   * @return the expectation annotation if found at method or class level, or null if not present
   */
  private Expectation findExpectation(final ExtensionContext context) {
    final var method = context.getRequiredTestMethod();
    return Optional.ofNullable(method.getAnnotation(Expectation.class))
        .orElseGet(() -> context.getRequiredTestClass().getAnnotation(Expectation.class));
  }

  /**
   * Gets or creates the Configuration for the current test class.
   *
   * <p>By default, uses {@link Configuration#defaults()}. Tests can customize configuration by
   * calling {@link #setConfiguration(ExtensionContext, Configuration)} before the first test runs.
   * The configuration is stored in the extension context store and shared across all tests in the
   * class hierarchy.
   *
   * @param extensionContext the extension context providing access to the configuration store
   * @return the configuration instance for this test class, either custom or default
   */
  private static Configuration getConfiguration(final ExtensionContext extensionContext) {
    return getOrCreate(
        extensionContext, STORE_KEY_CONFIGURATION, Configuration.class, Configuration::defaults);
  }

  /**
   * Generic get-or-create method for extension state management.
   *
   * <p>Retrieves an instance from the extension context store, creating and storing it if not
   * present. This pattern ensures lazy initialization and proper lifecycle management for per-class
   * extension state. The method is thread-safe through the extension context store mechanism.
   *
   * @param <T> the type of the instance to retrieve or create
   * @param extensionContext the extension context providing access to the store
   * @param key the storage key identifying the instance in the store
   * @param type the class of the instance for type-safe retrieval
   * @param factory the factory function to create new instances when not found in store
   * @return the existing instance from store, or newly created instance if not present
   */
  private static <T> T getOrCreate(
      final ExtensionContext extensionContext,
      final String key,
      final Class<T> type,
      final Supplier<T> factory) {
    final var store = getClassScopedStore(extensionContext);
    return Optional.ofNullable(store.get(key, type))
        .orElseGet(
            () -> {
              final var instance = factory.get();
              store.put(key, instance);
              return instance;
            });
  }

  /**
   * Returns the class-scoped store used to hold extension state for a specific test class.
   *
   * <p>For nested test classes, this method returns the store for the top-level test class to
   * ensure that state (such as data source registrations) is shared across all nested classes. The
   * store is created with a namespace unique to this extension and the top-level test class.
   *
   * @param extensionContext the current extension context providing access to test hierarchy
   * @return store scoped to the top-level test class, shared across nested test classes
   */
  private static Store getClassScopedStore(final ExtensionContext extensionContext) {
    final var topLevelTestClass = getTopLevelTestClass(extensionContext);
    final var namespace = Namespace.create(DatabaseTestExtension.class, topLevelTestClass);
    return extensionContext.getRoot().getStore(namespace);
  }

  /**
   * Finds the top-level test class by traversing up the context hierarchy.
   *
   * <p>For nested test classes, this method returns the outermost test class. For non-nested
   * classes, it returns the test class itself. This ensures configuration and state sharing across
   * the entire test class hierarchy.
   *
   * @param extensionContext the current extension context for hierarchy traversal
   * @return the top-level test class from the context hierarchy
   */
  private static Class<?> getTopLevelTestClass(final ExtensionContext extensionContext) {
    return findTopLevelContext(extensionContext).getRequiredTestClass();
  }

  /**
   * Recursively finds the top-level context by traversing up the parent hierarchy.
   *
   * <p>This method recursively navigates up the parent chain until it finds the root test context
   * with a test class. The recursion terminates when no parent with a test class exists.
   *
   * @param extensionContext the current extension context for recursive traversal
   * @return the top-level extension context at the root of the test hierarchy
   */
  private static ExtensionContext findTopLevelContext(final ExtensionContext extensionContext) {
    return extensionContext
        .getParent()
        .filter(parent -> parent.getTestClass().isPresent())
        .map(DatabaseTestExtension::findTopLevelContext)
        .orElse(extensionContext);
  }
}
