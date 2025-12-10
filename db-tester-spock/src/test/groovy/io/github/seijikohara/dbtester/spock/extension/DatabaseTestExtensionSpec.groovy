package io.github.seijikohara.dbtester.spock.extension

import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import spock.lang.Specification

/**
 * Unit tests for {@link DatabaseTestExtension}.
 *
 * <p>This specification verifies the annotation-driven Spock extension that handles
 * database testing annotations on specifications and feature methods.
 */
class DatabaseTestExtensionSpec extends Specification {

	/** The extension under test. */
	DatabaseTestExtension extension

	def setup() {
		extension = new DatabaseTestExtension()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new DatabaseTestExtension()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should implement IAnnotationDrivenExtension interface'() {
		expect: 'extension implements IAnnotationDrivenExtension'
		extension instanceof IAnnotationDrivenExtension
	}

	def 'should create multiple independent extensions'() {
		when: 'creating multiple extensions'
		def extension1 = new DatabaseTestExtension()
		def extension2 = new DatabaseTestExtension()

		then: 'extensions are independent'
		!extension1.is(extension2)
		extension1 instanceof IAnnotationDrivenExtension
		extension2 instanceof IAnnotationDrivenExtension
	}
}
