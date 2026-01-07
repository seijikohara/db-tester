package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrarSupport
import io.github.seijikohara.dbtester.spring.support.PrimaryBeanResolver
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Registers Spring-managed {@link DataSource} beans with a {@link DataSourceRegistry}.
 *
 * <p>This class acts as a bridge between the Spring application context and the database testing
 * framework. It discovers all {@link DataSource} beans in the context and delegates registration to
 * {@link DataSourceRegistrarSupport}.
 *
 * <p>The registrar discovers DataSource beans using the following rules: if a single DataSource
 * is found, it becomes the default; if multiple DataSources are found, the one marked with
 * {@code @Primary} becomes the default; all DataSources are registered by their bean names for
 * named access.
 *
 * @see DataSourceRegistry
 * @see DataSourceRegistrarSupport
 * @see DbTesterSpockAutoConfiguration
 */
final class DataSourceRegistrar implements ApplicationContextAware {

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
	 *   <li>Delegates to {@link DataSourceRegistrarSupport} for registration
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

		DataSourceRegistrarSupport.registerDataSources(
				registry,
				dataSources,
				{ name -> PrimaryBeanResolver.isPrimaryBean(context, name, logger) },
				logger
				)
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
