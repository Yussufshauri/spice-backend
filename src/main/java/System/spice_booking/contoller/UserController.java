package System.spice_booking.contoller;

import System.spice_booking.dto.LoginRequest;
import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import System.spice_booking.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
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

    // ===== Helper: remove password from responses =====
    private User sanitize(User u) {
        if (u != null) u.setPassword(null);
        return u;
    }

    private List<User> sanitizeList(List<User> users) {
        users.forEach(this::sanitize);
        return users;
    }

    private String clean(String s) {
        return s == null ? null : s.trim();
    }

    // ===== GET ALL USERS (password hidden intentionally) =====
    @GetMapping
    public List<User> getAllUsers() {
        return sanitizeList(repository.findAll());
    }

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        String username = clean(user.getUsername());
        String password = user.getPassword();
        String email = user.getEmail() != null ? user.getEmail().trim() : null;

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        if (repository.existsByUsername(username)) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        user.setUsername(username);
        user.setEmail(email);

        // ADMIN mmoja tu kwa email maalum
        if (email != null && "yussuf@gmail.com".equalsIgnoreCase(email)) {
            Optional<User> adminExist = repository.findByRole(Role.Admin);
            if (adminExist.isPresent()) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin already exists"));
            }
            user.setRole(Role.Admin);
        } else {
            user.setRole(Role.Tourist);
        }

        // ✅ HASH PASSWORD (no spring-security)
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
        user.setPassword(hashed);

        User saved = repository.save(user);
        return ResponseEntity.ok(sanitize(saved));
    }

    // ===== LOGIN (BCrypt check) =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String username = clean(request.getUsername());
        String password = request.getPassword();

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<User> userOpt = repository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User u = userOpt.get();

        // ✅ Support old users (plain password) + new users (bcrypt)
        // If stored password looks like bcrypt, use checkpw; otherwise compare plain then upgrade.
        String stored = u.getPassword();

        boolean ok;
        if (stored != null && stored.startsWith("$2a$") || stored != null && stored.startsWith("$2b$") || stored != null && stored.startsWith("$2y$")) {
            ok = BCrypt.checkpw(password, stored);
        } else {
            // Old plain-text stored (bad but exists)
            ok = Objects.equals(password, stored);

            // If ok, upgrade to bcrypt automatically
            if (ok) {
                String newHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
                u.setPassword(newHash);
                repository.save(u);
            }
        }

        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(sanitize(u));
    }

    // ===== UPDATE USER =====
    @PutMapping("/{user_id}")
    public ResponseEntity<?> updateUser(@PathVariable Long user_id, @RequestBody User updatedUser) {

        Optional<User> existingUserOpt = repository.findById(user_id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User existingUser = existingUserOpt.get();

        // username uniqueness
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
            String newUsername = clean(updatedUser.getUsername());

            Optional<User> userWithSameUsername = repository.findByUsername(newUsername);
            if (userWithSameUsername.isPresent()
                    && !userWithSameUsername.get().getUser_id().equals(user_id)) {
                return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
            }
            existingUser.setUsername(newUsername);
        }

        if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail().trim());

        // ✅ if password provided => hash it
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            String hashed = BCrypt.hashpw(updatedUser.getPassword(), BCrypt.gensalt(10));
            existingUser.setPassword(hashed);
        }

        // Role update (optional)
        if (updatedUser.getRole() != null) existingUser.setRole(updatedUser.getRole());

        User savedUser = repository.save(existingUser);
        return ResponseEntity.ok(sanitize(savedUser));
    }

    // ===== REGISTER GUIDE =====
    @PostMapping("/register-guide")
    public ResponseEntity<?> registerGuide(@RequestBody User user) {

        String username = clean(user.getUsername());
        String password = user.getPassword();
        String email = user.getEmail() != null ? user.getEmail().trim() : null;

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        if (repository.existsByUsername(username)) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(Role.Guide);

        // ✅ HASH
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));

        User saved = repository.save(user);
        return ResponseEntity.ok(sanitize(saved));
    }

    // ===== DELETE USER =====
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long user_id) {
        if (!repository.existsById(user_id)) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        repository.deleteById(user_id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}