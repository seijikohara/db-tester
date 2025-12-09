package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext

/**
 * Registers Spring-managed {@link DataSource} beans with a {@link DataSourceRegistry}.
 *
 * <p>This class acts as a bridge between the Spring application context and the database testing
 * framework. It discovers all {@link DataSource} beans in the context and provides methods to
 * register them with a {@link DataSourceRegistry}.
 *
 * <p>The registrar discovers DataSource beans using the following rules: if a single DataSource
 * is found, it becomes the default; if multiple DataSources are found, the one marked with
 * {@code @Primary} becomes the default; all DataSources are registered by their bean names for
 * named access.
 *
 * @see DataSourceRegistry
 * @see DbTesterSpockAutoConfiguration
 */
final class DataSourceRegistrar implements ApplicationContextAware {

	/** Default bean name used as fallback when no primary DataSource is found. */
	private static final String DEFAULT_DATASOURCE_BEAN_NAME = 'dataSource'

	/** Logger for tracking DataSource registration activity. */
	private static final Logger logger = LoggerFactory.getLogger(DataSourceRegistrar)

	/** Configuration properties for the registrar. */
	private final DbTesterProperties properties

	/** The Spring application context, set by {@link #setApplicationContext}. */
	private ApplicationContext applicationContext

	/**
	 * Creates a new registrar with the specified properties.
	 *
	 * @param properties the configuration properties
	 */
	DataSourceRegistrar(DbTesterProperties properties) {
		this.properties = properties
	}

	/**
	 * Sets the application context.
	 *
	 * <p>This method is called by Spring during bean initialization.
	 *
	 * @param applicationContext the Spring application context
	 * @throws BeansException if context setting fails
	 */
	@Override
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext
	}

	/**
	 * Registers all DataSource beans from the Spring context with the specified registry.
	 *
	 * <p>This method performs the following:
	 *
	 * <ol>
	 *   <li>Discovers all DataSource beans in the application context
	 *   <li>Registers the primary DataSource as the default
	 *   <li>Registers all DataSources by their bean names
	 * </ol>
	 *
	 * <p>If auto-registration is disabled in properties, this method does nothing.
	 *
	 * @param registry the DataSourceRegistry to populate
	 * @throws IllegalStateException if application context is not set
	 */
	void registerAll(DataSourceRegistry registry) {
		if (!properties.autoRegisterDataSources) {
			logger.debug('Auto-registration disabled')
			return
		}

		def context = resolveApplicationContext()
		def dataSources = context.getBeansOfType(DataSource)

		if (dataSources.empty) {
			logger.debug('No DataSource beans found')
			return
		}

		registerDataSources(registry, dataSources)
	}

	/**
	 * Registers the discovered DataSources with the registry.
	 *
	 * @param registry the registry to populate
	 * @param dataSources the map of bean names to DataSource instances
	 */
	private void registerDataSources(DataSourceRegistry registry, Map<String, DataSource> dataSources) {
		logger.info('Registering {} DataSource(s) with DataSourceRegistry', dataSources.size())

		// Register each DataSource by name
		dataSources.each { name, dataSource ->
			registry.register(name, dataSource)
			logger.debug("Registered DataSource '{}' with registry", name)
		}

		// Register default DataSource
		def defaultEntry = resolveDefaultDataSource(dataSources)
		if (defaultEntry) {
			registry.registerDefault(defaultEntry.value)
			logger.info("Registered DataSource '{}' as default", defaultEntry.key)
		}
	}

	/**
	 * Resolves the default DataSource from the discovered DataSources.
	 *
	 * <p>Resolution priority: single DataSource (automatic default), primary-annotated DataSource,
	 * DataSource named "dataSource".
	 *
	 * @param dataSources the map of discovered DataSources
	 * @return the default DataSource entry, or {@code null} if none found
	 */
	private Map.Entry<String, DataSource> resolveDefaultDataSource(Map<String, DataSource> dataSources) {
		// Single DataSource is automatically the default
		if (dataSources.size() == 1) {
			return dataSources.entrySet().first()
		}

		// Find primary DataSource
		def primaryEntry = findPrimaryDataSource(dataSources)
		if (primaryEntry) {
			return primaryEntry
		}

		// Find DataSource by default name
		return findDataSourceByName(dataSources, DEFAULT_DATASOURCE_BEAN_NAME)
	}

	/**
	 * Finds the primary DataSource bean from the context.
	 *
	 * @param dataSources the map of discovered DataSources
	 * @return the primary DataSource entry, or {@code null} if none found
	 */
	private Map.Entry<String, DataSource> findPrimaryDataSource(Map<String, DataSource> dataSources) {
		def context = resolveApplicationContext()

		return dataSources.find { name, ds ->
			context.containsBeanDefinition(name) && isPrimaryBean(context, name)
		}
	}

	/**
	 * Finds a DataSource by its bean name.
	 *
	 * @param dataSources the map of discovered DataSources
	 * @param beanName the bean name to search for
	 * @return the matching DataSource entry, or {@code null} if not found
	 */
	private Map.Entry<String, DataSource> findDataSourceByName(Map<String, DataSource> dataSources, String beanName) {
		return dataSources.find { name, ds -> name == beanName }
	}

	/**
	 * Checks if a bean is marked as primary.
	 *
	 * @param context the application context
	 * @param beanName the bean name to check
	 * @return {@code true} if the bean is primary, {@code false} otherwise
	 */
	private boolean isPrimaryBean(ApplicationContext context, String beanName) {
		if (context instanceof ConfigurableApplicationContext) {
			def factory = context.beanFactory
			if (factory.containsBeanDefinition(beanName)) {
				return factory.getBeanDefinition(beanName).primary
			}
		}
		logger.debug("Unable to determine if bean '{}' is primary", beanName)
		return false
	}

	/**
	 * Resolves the application context, throwing if not set.
	 *
	 * @return the application context
	 * @throws IllegalStateException if the context is not set
	 */
	private ApplicationContext resolveApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException(
			'ApplicationContext not set. ' +
			'Ensure this bean is managed by Spring and properly initialized.')
		}
		return applicationContext
	}
}
