package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import org.spockframework.runtime.extension.IGlobalExtension
import spock.lang.Specification

/**
 * Unit tests for {@link SpringBootDatabaseTestExtension}.
 *
 * <p>This specification verifies the Spring Boot-aware Spock extension
 * for database testing.
 *
 * <p>Note: Due to Spock's limitation on mocking final classes like Class, Method,
 * SpecInfo, and FeatureInfo, these tests focus on basic functionality verification.
 */
class SpringBootDatabaseTestExtensionSpec extends Specification {

	/** The extension under test. */
	SpringBootDatabaseTestExtension extension

	def setup() {
		extension = new SpringBootDatabaseTestExtension()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new SpringBootDatabaseTestExtension()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should implement IGlobalExtension interface'() {
		expect: 'extension implements IGlobalExtension'
		extension instanceof IGlobalExtension
	}

	def 'should not throw exception on start'() {
		when: 'calling start'
		extension.start()

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should not throw exception on stop'() {
		when: 'calling stop'
		extension.stop()

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should allow multiple start calls'() {
		when: 'calling start multiple times'
		extension.start()
		extension.start()

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should allow multiple stop calls'() {
		when: 'calling stop multiple times'
		extension.stop()
		extension.stop()

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should allow start and stop sequence'() {
		when: 'calling start then stop'
		extension.start()
		extension.stop()

		then: 'no exception is thrown'
		noExceptionThrown()
	}

	def 'should create multiple independent extensions'() {
		when: 'creating multiple extensions'
		def extension1 = new SpringBootDatabaseTestExtension()
		def extension2 = new SpringBootDatabaseTestExtension()

		then: 'extensions are independent'
		!extension1.is(extension2)
		extension1 instanceof IGlobalExtension
		extension2 instanceof IGlobalExtension
	}
}
