package System.spice_booking.repository;

import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);
    Optional<User> findByRole(Role role);
    boolean existsByUsername(String username);
}
