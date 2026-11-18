package com.cinebook.controller;

import com.cinebook.service.CineBookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    private final CineBookService service;
    public ApiController(CineBookService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","UP","application","CineBook"); }

    @GetMapping("/movies")
    public List<Map<String,Object>> movies() { return service.getMovies(); }

    @PostMapping("/movies")
    public ResponseEntity<?> addMovie(@Valid @RequestBody MovieRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMovie(r.title(),r.genre(),r.language(),r.durationMinutes(),r.releaseDate(),r.posterUrl(),r.description()));
    }

    @GetMapping("/shows")
    public List<Map<String,Object>> shows(@RequestParam(required=false) Long movieId) { return service.getShows(movieId); }

    @PostMapping("/shows")
    public ResponseEntity<?> addShow(@Valid @RequestBody ShowRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addShow(r.movieId(),r.screenId(),r.showDate(),r.showTime(),r.ticketPrice()));
    }

    @GetMapping("/shows/{showId}/seats")
    public List<Map<String,Object>> seats(@PathVariable long showId) { return service.getSeats(showId); }

    @PostMapping("/bookings")
    public ResponseEntity<?> book(@Valid @RequestBody BookingRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(r.customerName(),r.customerEmail(),r.showId(),r.seatIds()));
    }

    @GetMapping("/bookings/{bookingId}")
    public Map<String,Object> booking(@PathVariable long bookingId) { return service.getBooking(bookingId); }

    @PostMapping("/bookings/{bookingId}/cancel")
    public Map<String,Object> cancel(@PathVariable long bookingId) { return service.cancelBooking(bookingId); }

    public record MovieRequest(@NotBlank String title,@NotBlank String genre,@NotBlank String language,
        @Min(1) int durationMinutes,@NotNull LocalDate releaseDate,String posterUrl,String description) {}
    public record ShowRequest(@NotNull Long movieId,@NotNull Long screenId,@NotNull LocalDate showDate,
        @NotNull LocalTime showTime,@DecimalMin("0.01") BigDecimal ticketPrice) {}
    public record BookingRequest(@NotBlank String customerName,@Email @NotBlank String customerEmail,
        @NotNull Long showId,@NotEmpty List<@NotNull Long> seatIds) {}
}
