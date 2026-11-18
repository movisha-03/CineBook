
package com.cinebook.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class CineBookService {

    private final JdbcTemplate jdbc;

    public CineBookService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String,Object>> getMovies() {
        return jdbc.queryForList(
            "SELECT movie_id AS id,title,genre,language,duration_minutes AS durationMinutes,release_date AS releaseDate,poster_url AS posterUrl,description FROM movies WHERE active=TRUE ORDER BY movie_id DESC"
        );
    }

    public Map<String,Object> addMovie(
        String title,
        String genre,
        String language,
        int duration,
        LocalDate releaseDate,
        String posterUrl,
        String description
    ) {
        jdbc.update(
            "INSERT INTO movies(title,genre,language,duration_minutes,release_date,poster_url,description) VALUES(?,?,?,?,?,?,?)",
            title, genre, language, duration,
            Date.valueOf(releaseDate), posterUrl, description
        );

        return Map.of("message", "Movie added successfully");
    }

    // GET ALL ACTIVE SHOWS - NO DATE RESTRICTION
    public List<Map<String,Object>> getShows(Long movieId) {

        String sql = "SELECT s.show_id AS id,s.movie_id AS movieId,m.title AS movieTitle,s.screen_id AS screenId,sc.screen_name AS screenName,s.show_date AS showDate,s.show_time AS showTime,s.ticket_price AS ticketPrice,sc.capacity FROM shows s JOIN movies m ON m.movie_id=s.movie_id JOIN screens sc ON sc.screen_id=s.screen_id WHERE s.active=TRUE";

        return movieId == null
            ? jdbc.queryForList(sql + " ORDER BY s.show_date,s.show_time")
            : jdbc.queryForList(sql + " AND s.movie_id=? ORDER BY s.show_date,s.show_time", movieId);
    }

    public Map<String,Object> addShow(
        Long movieId,
        Long screenId,
        LocalDate date,
        LocalTime time,
        BigDecimal price
    ) {
        jdbc.update(
            "INSERT INTO shows(movie_id,screen_id,show_date,show_time,ticket_price) VALUES(?,?,?,?,?)",
            movieId, screenId, Date.valueOf(date),
            Time.valueOf(time), price
        );

        Long showId = jdbc.queryForObject(
            "SELECT LAST_INSERT_ID()", Long.class
        );

        jdbc.update(
            "INSERT INTO show_seats(show_id,seat_id,seat_number,seat_type,price,status) SELECT ?,seat_id,seat_number,seat_type,?,'AVAILABLE' FROM seats WHERE screen_id=?",
            showId, price, screenId
        );

        return Map.of(
            "message", "Show scheduled successfully",
            "showId", showId
        );
    }

    public List<Map<String,Object>> getSeats(long showId) {

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM shows WHERE show_id=?",
            Integer.class, showId
        );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Show not found"
            );
        }

        return jdbc.queryForList(
            "SELECT seat_id AS id,seat_number AS seatNumber,seat_type AS seatType,price,status FROM show_seats WHERE show_id=? ORDER BY seat_number",
            showId
        );
    }

    @Transactional
    public Map<String,Object> createBooking(
        String name,
        String email,
        long showId,
        List<Long> requested
    ) {
        List<Long> ids = requested.stream().distinct().sorted().toList();

        if (ids.size() != requested.size()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Duplicate seat selected"
            );
        }

        List<Map<String,Object>> seatRows = new ArrayList<>();

        for (Long id : ids) {

            List<Map<String,Object>> found = jdbc.queryForList(
                "SELECT seat_id,price,status FROM show_seats WHERE show_id=? AND seat_id=? FOR UPDATE",
                showId, id
            );

            if (found.isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat does not belong to this show"
                );
            }

            if (!"AVAILABLE".equals(found.get(0).get("status"))) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "One or more seats are no longer available"
                );
            }

            seatRows.add(found.get(0));
        }

        jdbc.update(
            "INSERT INTO customers(full_name,email) VALUES(?,?) ON DUPLICATE KEY UPDATE full_name=VALUES(full_name)",
            name, email
        );

        Long customerId = jdbc.queryForObject(
            "SELECT customer_id FROM customers WHERE email=?",
            Long.class, email
        );

        BigDecimal total = seatRows.stream()
            .map(r -> new BigDecimal(r.get("price").toString()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        jdbc.update(
            "INSERT INTO bookings(customer_id,show_id,booking_date,total_amount,booking_status) VALUES(?,?,NOW(),?,'CONFIRMED')",
            customerId, showId, total
        );

        Long bookingId = jdbc.queryForObject(
            "SELECT LAST_INSERT_ID()", Long.class
        );

        for (Map<String,Object> row : seatRows) {

            Long seatId = ((Number) row.get("seat_id")).longValue();

            jdbc.update(
                "INSERT INTO booking_seats(booking_id,show_id,seat_id,ticket_price) VALUES(?,?,?,?)",
                bookingId, showId, seatId, row.get("price")
            );

            jdbc.update(
                "UPDATE show_seats SET status='BOOKED' WHERE show_id=? AND seat_id=?",
                showId, seatId
            );
        }

        jdbc.update(
            "INSERT INTO payments(booking_id,amount,payment_method,payment_status,paid_at) VALUES(?,?,'DEMO','SUCCESS',NOW())",
            bookingId, total
        );

        return Map.of(
            "bookingId", bookingId,
            "customerName", name,
            "showId", showId,
            "seatIds", ids,
            "totalAmount", total,
            "bookingStatus", "CONFIRMED",
            "paymentStatus", "SUCCESS",
            "message", "Demo booking completed"
        );
    }

    public Map<String,Object> getBooking(long id) {

        List<Map<String,Object>> rows = jdbc.queryForList(
            "SELECT b.booking_id AS bookingId,c.full_name AS customerName,c.email,m.title AS movieTitle,s.show_date AS showDate,s.show_time AS showTime,b.total_amount AS totalAmount,b.booking_status AS bookingStatus,GROUP_CONCAT(st.seat_number ORDER BY st.seat_number SEPARATOR ', ') AS seats FROM bookings b JOIN customers c ON c.customer_id=b.customer_id JOIN shows s ON s.show_id=b.show_id JOIN movies m ON m.movie_id=s.movie_id LEFT JOIN booking_seats bs ON bs.booking_id=b.booking_id LEFT JOIN seats st ON st.seat_id=bs.seat_id WHERE b.booking_id=? GROUP BY b.booking_id",
            id
        );

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"
            );
        }

        return rows.get(0);
    }

    @Transactional
    public Map<String,Object> cancelBooking(long id) {

        List<Map<String,Object>> rows = jdbc.queryForList(
            "SELECT booking_id,show_id,booking_status,total_amount FROM bookings WHERE booking_id=? FOR UPDATE",
            id
        );

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"
            );
        }

        Map<String,Object> b = rows.get(0);

        if (!"CONFIRMED".equals(b.get("booking_status"))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Booking is not active"
            );
        }

        List<Map<String,Object>> seats = jdbc.queryForList(
            "SELECT seat_id FROM booking_seats WHERE booking_id=?",
            id
        );

        for (Map<String,Object> seat : seats) {
            jdbc.update(
                "UPDATE show_seats SET status='AVAILABLE' WHERE show_id=? AND seat_id=?",
                b.get("show_id"), seat.get("seat_id")
            );
        }

        jdbc.update(
            "UPDATE bookings SET booking_status='CANCELLED' WHERE booking_id=?",
            id
        );

        jdbc.update(
            "UPDATE payments SET payment_status='REFUNDED' WHERE booking_id=? AND payment_status='SUCCESS'",
            id
        );

        jdbc.update(
            "INSERT INTO refunds(booking_id,refund_amount,refund_status) VALUES(?,?,'COMPLETED')",
            id, b.get("total_amount")
        );

        return Map.of(
            "bookingId", id,
            "bookingStatus", "CANCELLED",
            "refundAmount", b.get("total_amount"),
            "refundStatus", "REFUNDED"
        );
    }
}
