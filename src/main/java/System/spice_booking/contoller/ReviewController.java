package System.spice_booking.contoller;

import System.spice_booking.model.entity.Review;
import System.spice_booking.model.entity.Tour;
import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import System.spice_booking.repository.ReviewRepository;
import System.spice_booking.repository.TourRepository;
import System.spice_booking.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    // GET ALL REVIEWS
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // GET REVIEWS BY TOUR
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<?> getReviewsByTour(@PathVariable Long tourId) {
        if (!tourRepository.existsById(tourId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Tour not found"));
        }
        return ResponseEntity.ok(reviewRepository.findByTourId(tourId));
    }

    // CREATE REVIEW (Tourist only)
    @PostMapping("/create")
    public ResponseEntity<?> createReview(
            @RequestParam Long userId,
            @RequestParam Long tourId,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Tour> optionalTour = tourRepository.findById(tourId);

        if (optionalUser.isEmpty() || optionalTour.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Tour not found"));
        }

        User user = optionalUser.get();

        if (user.getRole() != Role.Tourist) {
            return ResponseEntity.status(403).body(Map.of("error", "Only tourists can leave reviews"));
        }

        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }

        if (comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment is required"));
        }

        Review review = new Review();
        review.setUser(user);
        review.setTour(optionalTour.get());
        review.setRating(rating);
        review.setComment(comment);

        // If you changed Review.reviewDate to LocalDate:
        try {
            review.getClass().getMethod("setReviewDate", LocalDate.class).invoke(review, LocalDate.now());
        } catch (Exception ignored) {
            review.setReviewDate(LocalDate.now().toString());
        }

        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }

    // DELETE REVIEW
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Review not found"));
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }
}