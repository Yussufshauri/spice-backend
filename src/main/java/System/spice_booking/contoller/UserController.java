package System.spice_booking.contoller;

import System.spice_booking.dto.LoginRequest;
import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import System.spice_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserRepository repository;

    //GET
    @GetMapping
    public List<User> getAllUsers(){
        return repository.findAll();
    }

    //POST/CREATE
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){

        Optional<User> existUser = repository.findByUsername(user.getUsername());
        if (existUser.isPresent()){
            return ResponseEntity.status(409).body("Username already exists");
        }

        // ADMIN mmoja tu kwa email maalum
        if ("yussuf@gmail.com".equalsIgnoreCase(user.getEmail())) {

            Optional<User> adminExist = repository.findByRole(Role.Admin);
            if (adminExist.isPresent()) {
                return ResponseEntity.status(403).body("Admin already exists");
            }

            user.setRole(Role.Admin);

        } else {
            // DEFAULT ROLE
            user.setRole(Role.Tourist);
        }

        return ResponseEntity.ok(repository.save(user));
    }

    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userCredentials = repository.findByUsernameAndPassword(request.getUsername(),request.getPassword());
        if (userCredentials.isPresent()) {
            return ResponseEntity.ok(userCredentials.get());
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
    // UPDATE USER
    @PutMapping("/{user_id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long user_id,
            @RequestBody User updatedUser
    ) {
        Optional<User> existingUserOpt = repository.findById(user_id);

        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User existingUser = existingUserOpt.get();

        // Optional: check username uniqueness
        Optional<User> userWithSameUsername = repository.findByUsername(updatedUser.getUsername());
        if (userWithSameUsername.isPresent() &&
                !userWithSameUsername.get().getUser_id().equals(user_id)) {
            return ResponseEntity.status(409).body("Username already exists");
        }

        // Update fields
        existingUser.setName(updatedUser.getName());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());

        User savedUser = repository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }

    //register Guider
    @PostMapping("/register-guide")
    public ResponseEntity<?> registerGuide(@RequestBody User user){

        Optional<User> existUser = repository.findByUsername(user.getUsername());
        if (existUser.isPresent()){
            return ResponseEntity.status(409).body("Username already exists");
        }

        user.setRole(Role.Guide);
        return ResponseEntity.ok(repository.save(user));
    }

    //DELETE
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long user_id){
        if (!repository.existsById(user_id)) {
            return ResponseEntity.status(404).body("User not found");
        }
        repository.deleteById(user_id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
