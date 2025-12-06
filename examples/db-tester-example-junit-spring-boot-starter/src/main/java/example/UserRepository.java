package example;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for User entity operations.
 *
 * <p>This repository demonstrates Spring Data JPA integration with the database testing framework.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds all users ordered by their identifier.
   *
   * @return a list of all users ordered by id
   */
  List<User> findAllByOrderByIdAsc();
}
