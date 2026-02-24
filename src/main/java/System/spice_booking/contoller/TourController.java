package System.spice_booking.contoller;

import System.spice_booking.model.entity.Tour;
import System.spice_booking.model.entity.User;
import System.spice_booking.repository.TourRepository;
import System.spice_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api/tour")
@CrossOrigin(origins = "*")
public class TourController {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Better: use absolute safe folder + create nested dirs
    private final Path uploadDir = Paths.get("uploads");

    @GetMapping
    public java.util.List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTour(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam String date,
            @RequestParam Long userId,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            // ✅ Validate user
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            User user = optionalUser.get();

            // ✅ allow only ADMIN or GUIDE
            if (!user.getRole().name().equals("Admin") && !user.getRole().name().equals("Guide")) {
                return ResponseEntity.status(403).body("Only Admin or Guide can create tours");
            }

            // ✅ Validate image
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body("Image is required");
            }

            // ✅ Validate date format (yyyy-MM-dd)
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date); // expects yyyy-MM-dd
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd (example: 2026-03-20)");
            }

            // ✅ Ensure folder exists (mkdirs)
            Files.createDirectories(uploadDir);

            // ✅ Clean filename
            String originalName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename());
            originalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

            String fileName = System.currentTimeMillis() + "_" + originalName;
            Path targetPath = uploadDir.resolve(fileName);

            // ✅ Save file
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // ✅ Build entity
            Tour tour = new Tour();
            tour.setTitle(title);
            tour.setDescription(description);
            tour.setPrice(String.valueOf(price));
            tour.setDate(parsedDate);
            tour.setImageUrl("uploads/" + fileName); // IMPORTANT: url served by WebConfig
            tour.setUser(user);

            tourRepository.save(tour);

            return ResponseEntity.ok(tour);

        } catch (Exception e) {
            // ✅ Now you will see the real error in console
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Create tour failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTour(@PathVariable Long id) {
        tourRepository.deleteById(id);
        return ResponseEntity.ok("Tour deleted");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable Long id, @RequestBody Tour updatedTour) {
        Optional<Tour> optionalTour = tourRepository.findById(id);
        if (optionalTour.isEmpty()) {
            return ResponseEntity.badRequest().body("Tour not found");
        }

        Tour tour = optionalTour.get();
        tour.setTitle(updatedTour.getTitle());
        tour.setDescription(updatedTour.getDescription());
        tour.setPrice(updatedTour.getPrice());
        tour.setDate(updatedTour.getDate());

        tourRepository.save(tour);
        return ResponseEntity.ok("Tour updated successfully");
    }
}