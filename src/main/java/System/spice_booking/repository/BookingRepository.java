
package System.spice_booking.repository;

import System.spice_booking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @org.springframework.data.jpa.repository.Query(
            "SELECT b FROM Booking b WHERE b.user.user_id = :userId"
    )
    List<Booking> findByUserUser_id(@org.springframework.data.repository.query.Param("userId") Long userId);
}
