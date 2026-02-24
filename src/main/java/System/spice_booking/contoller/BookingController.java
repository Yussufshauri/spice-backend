package System.spice_booking.contoller;

import System.spice_booking.model.entity.Booking;
import System.spice_booking.model.entity.Tour;
import System.spice_booking.model.entity.User;
import System.spice_booking.model.enums.Role;
import System.spice_booking.model.enums.Status;
import System.spice_booking.repository.BookingRepository;
import System.spice_booking.repository.TourRepository;
import System.spice_booking.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    // GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // GET BOOKINGS BY USER (Tourist - My Bookings page)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(bookingRepository.findByUserUser_id(userId));
    }


    // CREATE BOOKING (Tourist only)
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(
            @RequestParam Long userId,
            @RequestParam Long tourId,
            @RequestParam String date
    ) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Tour> optionalTour = tourRepository.findById(tourId);

        if (optionalUser.isEmpty() || optionalTour.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Tour not found"));
        }

        User user = optionalUser.get();

        if (user.getRole() != Role.Tourist) {
            return ResponseEntity.status(403).body(Map.of("error", "Only tourists can book tours"));
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTour(optionalTour.get());
        booking.setDate(date); // If you changed Booking.date to LocalDate, use LocalDate.parse(date)
        booking.setStatus(Status.Pending);

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.ok(saved);
    }

    // APPROVE BOOKING
    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveBooking(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        }

        Booking booking = optionalBooking.get();
        booking.setStatus(Status.Approved);
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Booking approved"));
    }

    // REJECT BOOKING
    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        }

        Booking booking = optionalBooking.get();
        booking.setStatus(Status.Rejected);
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Booking rejected"));
    }

    // GENERIC STATUS UPDATE (Admin/Guide dashboard friendly)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam Status status) {
        Optional<Booking> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        }
        Booking booking = opt.get();
        booking.setStatus(status);
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("message", "Booking status updated", "status", status.name()));
    }

    // DELETE BOOKING
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Booking deleted"));
    }
}