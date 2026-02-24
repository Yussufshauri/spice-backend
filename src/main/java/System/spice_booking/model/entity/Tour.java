
package System.spice_booking.model.entity;

import System.spice_booking.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tours")
@JsonIgnoreProperties({"user","reviews"})
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tour_id;
    private String name;
    private String description;
    private String price;
    private String location;
    private Status status = Status.Pending;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password","tours","booking","reviews"})
    private User user;
    private String title;
    private LocalDate date;
    private String imageUrl;
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    private List<Review> reviews;

    public Tour() {
    }

    public Tour(Long tour_id, String name, String description, String price, String location, Status status, User user, String title, LocalDate date, String imageUrl, List<Review> reviews) {
        this.tour_id = tour_id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.location = location;
        this.status = status;
        this.user = user;
        this.title = title;
        this.date = date;
        this.imageUrl = imageUrl;
        this.reviews = reviews;
    }

    public Long getTour_id() {
        return tour_id;
    }

    public void setTour_id(Long tour_id) {
        this.tour_id = tour_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
