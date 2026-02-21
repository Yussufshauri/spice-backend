package System.spice_booking.model.entity;

import System.spice_booking.model.enums.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String date;
    private Status status;

    public Booking() {}

    public Booking(Long booking_id, User user, String date, Status status) {
        this.booking_id = booking_id;
        this.user=user;
        this.date = date;
        this.status = status;
    }

    public Long getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(Long booking_id) {
        this.booking_id = booking_id;
    }

    public User getTourist() {
        return user;
    }

    public void setTourist(User tourist) {
        this.user = tourist;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
