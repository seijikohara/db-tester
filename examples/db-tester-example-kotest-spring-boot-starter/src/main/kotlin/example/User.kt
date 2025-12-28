package example

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * User entity for database testing demonstration.
 *
 * This is a JPA entity representing a user record in the USERS table.
 *
 * @property id the user identifier
 * @property name the user name
 * @property email the user email address
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    val id: Long? = null,
    @Column(nullable = false)
    val name: String = "",
    @Column(nullable = false)
    val email: String = "",
)
