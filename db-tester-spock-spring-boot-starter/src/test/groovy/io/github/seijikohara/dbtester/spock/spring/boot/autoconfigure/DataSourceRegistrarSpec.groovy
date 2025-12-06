package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import spock.lang.Specification

/**
 * Unit tests for {@link DataSourceRegistrar}.
 *
 * <p>This specification verifies the DataSource registration from
 * Spring application context to DataSourceRegistry.
 */
class DataSourceRegistrarSpec extends Specification {

	/** The registrar under test. */
	DataSourceRegistrar registrar

	/** Test properties. */
	DbTesterProperties properties

	/** Test registry. */
	DataSourceRegistry registry

	def setup() {
		properties = new DbTesterProperties()
		registrar = new DataSourceRegistrar(properties)
		registry = new DataSourceRegistry()
	}

	def 'should create instance with properties'() {
		given: 'custom properties'
		def customProperties = new DbTesterProperties()
		customProperties.enabled = false

		when: 'creating registrar'
		def customRegistrar = new DataSourceRegistrar(customProperties)

		then: 'instance is created successfully'
		customRegistrar != null
	}

	def 'should accept application context'() {
		given: 'a mock application context'
		def context = Mock(ConfigurableApplicationContext)

		when: 'setting application context'
		registrar.setApplicationContext(context)

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should throw IllegalStateException when context is not set'() {
		when: 'calling registerAll without setting context'
		registrar.registerAll(registry)

		then: 'IllegalStateException is thrown'
		thrown(IllegalStateException)
	}

	def 'should do nothing when auto-register is disabled'() {
		given: 'auto-register is disabled'
		properties.autoRegisterDataSources = false

		and: 'a mock context'
		def context = Mock(ConfigurableApplicationContext)
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'no DataSources are registered'
		!registry.hasDefault()
	}

	def 'should register single DataSource as default'() {
		given: 'a context with a single DataSource'
		def dataSource = Mock(DataSource)
		def context = createContextWithDataSources([dataSource: dataSource])
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'DataSource is registered as default'
		registry.hasDefault()
		registry.getDefault() == dataSource
		registry.has('dataSource')
	}

	def 'should register multiple DataSources by name'() {
		given: 'a context with multiple DataSources'
		def ds1 = Mock(DataSource)
		def ds2 = Mock(DataSource)
		def context = createContextWithDataSources([ds1: ds1, ds2: ds2])
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'all DataSources are registered by name'
		registry.has('ds1')
		registry.has('ds2')
		registry.get('ds1') == ds1
		registry.get('ds2') == ds2
	}

	def 'should register primary DataSource as default'() {
		given: 'a context with primary DataSource'
		def primaryDs = Mock(DataSource)
		def secondaryDs = Mock(DataSource)
		def context = createContextWithPrimaryDataSource('primaryDs', primaryDs, 'secondaryDs', secondaryDs)
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'primary DataSource is registered as default'
		registry.hasDefault()
		registry.getDefault() == primaryDs
	}

	def 'should fall back to dataSource bean name when no primary'() {
		given: 'a context with dataSource bean'
		def defaultDs = Mock(DataSource)
		def otherDs = Mock(DataSource)
		def context = createContextWithDataSources([dataSource: defaultDs, otherDs: otherDs])
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'dataSource bean is registered as default'
		registry.hasDefault()
		registry.getDefault() == defaultDs
	}

	def 'should handle empty DataSource map'() {
		given: 'a context with no DataSources'
		def context = createContextWithDataSources([:])
		registrar.setApplicationContext(context)

		when: 'calling registerAll'
		registrar.registerAll(registry)

		then: 'no DataSources are registered'
		!registry.hasDefault()
	}

	/**
	 * Creates a mock ConfigurableApplicationContext with the given DataSources.
	 *
	 * @param dataSources map of bean names to DataSources
	 * @return the mock context
	 */
	private ConfigurableApplicationContext createContextWithDataSources(Map<String, DataSource> dataSources) {
		def context = Mock(ConfigurableApplicationContext)
		def beanFactory = Mock(DefaultListableBeanFactory)

		context.getBeansOfType(DataSource) >> dataSources
		context.getBeanFactory() >> beanFactory

		dataSources.each { name, ds ->
			context.containsBeanDefinition(name) >> true
			beanFactory.containsBeanDefinition(name) >> true
			def beanDef = Mock(BeanDefinition)
			beanDef.isPrimary() >> false
			beanFactory.getBeanDefinition(name) >> beanDef
		}

		return context
	}

	/**
	 * Creates a mock ConfigurableApplicationContext with a primary DataSource.
	 *
	 * @param primaryName the primary DataSource bean name
	 * @param primaryDs the primary DataSource
	 * @param secondaryName the secondary DataSource bean name
	 * @param secondaryDs the secondary DataSource
	 * @return the mock context
	 */
	private ConfigurableApplicationContext createContextWithPrimaryDataSource(
			String primaryName,
			DataSource primaryDs,
			String secondaryName,
			DataSource secondaryDs) {
		def context = Mock(ConfigurableApplicationContext)
		def beanFactory = Mock(DefaultListableBeanFactory)

		context.getBeansOfType(DataSource) >> [(primaryName): primaryDs, (secondaryName): secondaryDs]
		context.getBeanFactory() >> beanFactory

		// Primary DataSource
		context.containsBeanDefinition(primaryName) >> true
		beanFactory.containsBeanDefinition(primaryName) >> true
		def primaryBeanDef = Mock(BeanDefinition)
		primaryBeanDef.isPrimary() >> true
		beanFactory.getBeanDefinition(primaryName) >> primaryBeanDef

		// Secondary DataSource
		context.containsBeanDefinition(secondaryName) >> true
		beanFactory.containsBeanDefinition(secondaryName) >> true
		def secondaryBeanDef = Mock(BeanDefinition)
		secondaryBeanDef.isPrimary() >> false
		beanFactory.getBeanDefinition(secondaryName) >> secondaryBeanDef

		return context
	}
}
