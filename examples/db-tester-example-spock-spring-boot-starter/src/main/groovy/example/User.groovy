package example

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * User entity for database testing demonstration.
 *
 * <p>This is a simple JPA entity representing a user record in the USERS table.
 */
@Entity
@Table(name = 'users')
@EqualsAndHashCode(includes = ['id'])
@ToString(includeNames = true, includePackage = false)
class User {

	/** The user identifier. */
	@Id
	Long id

	/** The user name. */
	@Column(nullable = false)
	String name

	/** The user email address. */
	@Column(nullable = false)
	String email

	/**
	 * Default constructor required by JPA.
	 */
	protected User() {}

	/**
	 * Creates a new user with the specified attributes.
	 *
	 * @param id the user identifier
	 * @param name the user name
	 * @param email the user email address
	 */
	User(Long id, String name, String email) {
		this.id = id
		this.name = name
		this.email = email
	}
}
