package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import javax.sql.DataSource

/**
 * Unit tests for [DataSourceRegistrar].
 *
 * This specification verifies the DataSource registration from
 * Spring application context to DataSourceRegistry.
 */
class DataSourceRegistrarSpec : AnnotationSpec() {
    /** The registrar under test. */
    private lateinit var registrar: DataSourceRegistrar

    /** Test properties. */
    private lateinit var properties: DbTesterProperties

    /** Test registry. */
    private lateinit var registry: DataSourceRegistry

    @BeforeEach
    fun setup(): Unit =
        run {
            properties = DbTesterProperties()
            registrar = DataSourceRegistrar(properties)
            registry = DataSourceRegistry()
        }

    @Test
    fun `should create instance with properties`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.isEnabled = false
            }.let { customProperties ->
                DataSourceRegistrar(customProperties).let { customRegistrar ->
                    customRegistrar shouldNotBe null
                }
            }

    @Test
    fun `should accept application context`(): Unit =
        mockk<ConfigurableApplicationContext>().let { context ->
            shouldNotThrowAny {
                registrar.setApplicationContext(context)
            }
        }

    @Test
    fun `should throw IllegalStateException when context is not set`(): Unit =
        run {
            shouldThrow<IllegalStateException> {
                registrar.registerAll(registry)
            }
        }

    @Test
    fun `should do nothing when auto-register is disabled`(): Unit =
        DbTesterProperties()
            .also { props ->
                props.isAutoRegisterDataSources = false
            }.let { props ->
                DataSourceRegistrar(props).let { reg ->
                    mockk<ConfigurableApplicationContext>().let { context ->
                        reg.setApplicationContext(context)
                        reg.registerAll(registry)
                        registry.hasDefault() shouldBe false
                    }
                }
            }

    @Test
    fun `should register single DataSource as default`(): Unit =
        mockk<DataSource>().let { dataSource ->
            createContextWithDataSources(mapOf("dataSource" to dataSource)).let { context ->
                registrar.setApplicationContext(context)
                registrar.registerAll(registry)
                registry.hasDefault() shouldBe true
                registry.getDefault() shouldBe dataSource
                registry.has("dataSource") shouldBe true
            }
        }

    @Test
    fun `should register multiple DataSources by name`(): Unit =
        mockk<DataSource>().let { ds1 ->
            mockk<DataSource>().let { ds2 ->
                createContextWithDataSources(mapOf("ds1" to ds1, "ds2" to ds2)).let { context ->
                    registrar.setApplicationContext(context)
                    registrar.registerAll(registry)
                    registry.has("ds1") shouldBe true
                    registry.has("ds2") shouldBe true
                    registry.get("ds1") shouldBe ds1
                    registry.get("ds2") shouldBe ds2
                }
            }
        }

    @Test
    fun `should register primary DataSource as default`(): Unit =
        mockk<DataSource>().let { primaryDs ->
            mockk<DataSource>().let { secondaryDs ->
                createContextWithPrimaryDataSource("primaryDs", primaryDs, "secondaryDs", secondaryDs).let { context ->
                    registrar.setApplicationContext(context)
                    registrar.registerAll(registry)
                    registry.hasDefault() shouldBe true
                    registry.getDefault() shouldBe primaryDs
                }
            }
        }

    @Test
    fun `should fall back to dataSource bean name when no primary`(): Unit =
        mockk<DataSource>().let { defaultDs ->
            mockk<DataSource>().let { otherDs ->
                createContextWithDataSources(mapOf("dataSource" to defaultDs, "otherDs" to otherDs)).let { context ->
                    registrar.setApplicationContext(context)
                    registrar.registerAll(registry)
                    registry.hasDefault() shouldBe true
                    registry.getDefault() shouldBe defaultDs
                }
            }
        }

    @Test
    fun `should handle empty DataSource map`(): Unit =
        createContextWithDataSources(emptyMap()).let { context ->
            registrar.setApplicationContext(context)
            registrar.registerAll(registry)
            registry.hasDefault() shouldBe false
        }

    /**
     * Creates a mock ConfigurableApplicationContext with the given DataSources.
     *
     * @param dataSources map of bean names to DataSources
     * @return the mock context
     */
    private fun createContextWithDataSources(dataSources: Map<String, DataSource>): ConfigurableApplicationContext =
        mockk<ConfigurableApplicationContext>(relaxed = true).also { context ->
            mockk<ConfigurableListableBeanFactory>(relaxed = true).also { beanFactory ->
                every { context.getBeansOfType(DataSource::class.java) } returns dataSources
                every { context.beanFactory } returns beanFactory
                dataSources.forEach { (name, _) ->
                    every { context.containsBeanDefinition(name) } returns true
                    every { beanFactory.containsBeanDefinition(name) } returns true
                    mockk<BeanDefinition>(relaxed = true).also { beanDef ->
                        every { beanDef.isPrimary } returns false
                        every { beanFactory.getBeanDefinition(name) } returns beanDef
                    }
                }
            }
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
    private fun createContextWithPrimaryDataSource(
        primaryName: String,
        primaryDs: DataSource,
        secondaryName: String,
        secondaryDs: DataSource,
    ): ConfigurableApplicationContext =
        mockk<ConfigurableApplicationContext>(relaxed = true).also { context ->
            mockk<ConfigurableListableBeanFactory>(relaxed = true).also { beanFactory ->
                every { context.getBeansOfType(DataSource::class.java) } returns
                    mapOf(primaryName to primaryDs, secondaryName to secondaryDs)
                every { context.beanFactory } returns beanFactory

                // Primary DataSource
                every { context.containsBeanDefinition(primaryName) } returns true
                every { beanFactory.containsBeanDefinition(primaryName) } returns true
                mockk<BeanDefinition>(relaxed = true).also { primaryBeanDef ->
                    every { primaryBeanDef.isPrimary } returns true
                    every { beanFactory.getBeanDefinition(primaryName) } returns primaryBeanDef
                }

                // Secondary DataSource
                every { context.containsBeanDefinition(secondaryName) } returns true
                every { beanFactory.containsBeanDefinition(secondaryName) } returns true
                mockk<BeanDefinition>(relaxed = true).also { secondaryBeanDef ->
                    every { secondaryBeanDef.isPrimary } returns false
                    every { beanFactory.getBeanDefinition(secondaryName) } returns secondaryBeanDef
                }
            }
        }
}
