package example

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository for User entity operations.
 *
 * This repository demonstrates Spring Data JPA integration with the database testing framework.
 */
interface UserRepository : JpaRepository<User, Long> {
    /**
     * Finds all users ordered by their identifier.
     *
     * @return a list of all users ordered by id
     */
    fun findAllByOrderByIdAsc(): List<User>
}
