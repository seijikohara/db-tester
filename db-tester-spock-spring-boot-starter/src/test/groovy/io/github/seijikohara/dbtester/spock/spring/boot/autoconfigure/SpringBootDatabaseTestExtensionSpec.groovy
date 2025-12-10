package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import spock.lang.Specification

/**
 * Unit tests for {@link SpringBootDatabaseTestExtension}.
 *
 * <p>This specification verifies the annotation-driven Spock extension
 * for Spring Boot database testing.
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

	def 'should implement IAnnotationDrivenExtension interface'() {
		expect: 'extension implements IAnnotationDrivenExtension'
		extension instanceof IAnnotationDrivenExtension
	}

	def 'should create multiple independent extensions'() {
		when: 'creating multiple extensions'
		def extension1 = new SpringBootDatabaseTestExtension()
		def extension2 = new SpringBootDatabaseTestExtension()

		then: 'extensions are independent'
		!extension1.is(extension2)
		extension1 instanceof IAnnotationDrivenExtension
		extension2 instanceof IAnnotationDrivenExtension
	}
}
