package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrarSupport
import io.github.seijikohara.dbtester.spring.support.PrimaryBeanResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import javax.sql.DataSource

/**
 * Registers Spring-managed [DataSource] beans with a [DataSourceRegistry].
 *
 * This class acts as a bridge between the Spring application context and the database testing
 * framework. It discovers all [DataSource] beans in the context and delegates registration to
 * [DataSourceRegistrarSupport].
 *
 * The registrar discovers DataSource beans using the following rules:
 * - If a single DataSource is found, it becomes the default
 * - If multiple DataSources are found, the one marked with `@Primary` becomes the default
 * - All DataSources are registered by their bean names for named access
 *
 * @see DataSourceRegistry
 * @see DataSourceRegistrarSupport
 * @see DbTesterKotestAutoConfiguration
 */
class DataSourceRegistrar(
    private val properties: DbTesterProperties,
) : ApplicationContextAware {
    /** Companion object containing class-level constants and logger. */
    companion object {
        private val logger = LoggerFactory.getLogger(DataSourceRegistrar::class.java)
    }

    private var applicationContext: ApplicationContext? = null

    /**
     * Sets the application context.
     *
     * This method is called by Spring during bean initialization.
     *
     * @param applicationContext the Spring application context
     * @throws BeansException if context setting fails
     */
    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext): Unit = run { this.applicationContext = applicationContext }

    /**
     * Registers all DataSource beans from the Spring context with the specified registry.
     *
     * This method performs the following:
     * 1. Discovers all DataSource beans in the application context
     * 2. Delegates to [DataSourceRegistrarSupport] for registration
     *
     * If auto-registration is disabled in properties, this method does nothing.
     *
     * @param registry the DataSourceRegistry to populate
     * @throws IllegalStateException if application context is not set
     */
    fun registerAll(registry: DataSourceRegistry): Unit =
        when {
            !properties.isAutoRegisterDataSources -> logger.debug("Auto-registration disabled")
            else ->
                requireApplicationContext().let { context ->
                    context
                        .getBeansOfType(DataSource::class.java)
                        .takeIf { it.isNotEmpty() }
                        ?.also { dataSources ->
                            DataSourceRegistrarSupport.registerDataSources(
                                registry,
                                dataSources,
                                { name -> PrimaryBeanResolver.isPrimaryBean(context, name, logger) },
                                logger,
                            )
                        }
                        ?: logger.debug("No DataSource beans found")
                }
        }

    /**
     * Requires the application context to be set.
     *
     * @return the application context
     * @throws IllegalStateException if application context is not set
     */
    private fun requireApplicationContext(): ApplicationContext =
        applicationContext
            ?: throw IllegalStateException(
                "ApplicationContext not set. Ensure this bean is managed by Spring and properly initialized.",
            )
}
