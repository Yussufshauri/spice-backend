package System.spice_booking.contoller;

import System.spice_booking.dto.LoginRequest;
import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import System.spice_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(
        origins = "https://spice-booking.netlify.app",
        allowCredentials = "true"
)
public class UserController {

    @Autowired
    private UserRepository repository;

    // Helper: remove password from responses
    private User sanitize(User u) {
        if (u != null) u.setPassword(null);
        return u;
    }

    private List<User> sanitizeList(List<User> users) {
        users.forEach(this::sanitize);
        return users;
    }

    // GET ALL USERS
    @GetMapping
    public List<User> getAllUsers() {
        return sanitizeList(repository.findAll());
    }

    // REGISTER (Default Tourist, Admin only if email matches and not exists)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<User> existUser = repository.findByUsername(user.getUsername());
        if (existUser.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        // ADMIN mmoja tu kwa email maalum
        if (user.getEmail() != null && "yussuf@gmail.com".equalsIgnoreCase(user.getEmail())) {
            Optional<User> adminExist = repository.findByRole(Role.Admin);
            if (adminExist.isPresent()) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin already exists"));
            }
            user.setRole(Role.Admin);
        } else {
            user.setRole(Role.Tourist);
        }

        User saved = repository.save(user);
        return ResponseEntity.ok(sanitize(saved));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<User> userCredentials =
                repository.findByUsernameAndPassword(request.getUsername(), request.getPassword());

        if (userCredentials.isPresent()) {
            return ResponseEntity.ok(sanitize(userCredentials.get()));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    // UPDATE USER
    @PutMapping("/{user_id}")
    public ResponseEntity<?> updateUser(@PathVariable Long user_id, @RequestBody User updatedUser) {

        Optional<User> existingUserOpt = repository.findById(user_id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User existingUser = existingUserOpt.get();

        // Check username uniqueness (if username provided)
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
            Optional<User> userWithSameUsername = repository.findByUsername(updatedUser.getUsername());
            if (userWithSameUsername.isPresent()
                    && !userWithSameUsername.get().getUser_id().equals(user_id)) {
                return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
            }
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        // Role update (optional). Kama hutaki role ibadilishwe ovyo, unaweza kuiondoa.
        if (updatedUser.getRole() != null) existingUser.setRole(updatedUser.getRole());

        User savedUser = repository.save(existingUser);
        return ResponseEntity.ok(sanitize(savedUser));
    }

    // REGISTER GUIDE
    @PostMapping("/register-guide")
    public ResponseEntity<?> registerGuide(@RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<User> existUser = repository.findByUsername(user.getUsername());
        if (existUser.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        user.setRole(Role.Guide);
        User saved = repository.save(user);
        return ResponseEntity.ok(sanitize(saved));
    }

    // DELETE USER
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long user_id) {
        if (!repository.existsById(user_id)) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        repository.deleteById(user_id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}