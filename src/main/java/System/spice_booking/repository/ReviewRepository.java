package System.spice_booking.repository;

import System.spice_booking.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.tour.tour_id = :tourId")
    List<Review> findByTourId(@Param("tourId") Long tourId);

}
