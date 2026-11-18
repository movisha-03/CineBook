# CineBook – Movie Theatre Ticket Booking & Box Office Analysis

College-project starter using Java 21, Spring Boot, MySQL, HTML/CSS/JavaScript and Python.

## Setup
1. Install JDK 21, Maven 3.9+, MySQL 8+, Python 3.10+ and VS Code/IntelliJ.
2. Run `database/cinebook_schema.sql` in MySQL Workbench.
3. Set MySQL credentials in `backend/src/main/resources/application.properties`.
4. From `backend`, run `mvn spring-boot:run`.
5. Open `http://localhost:8080`.
6. For analytics: `cd analytics`, create/activate a venv, run `pip install -r requirements.txt`, then `python analysis.py`.

## APIs
- GET `/api/health`
- GET `/api/movies`
- POST `/api/movies`
- GET `/api/shows?movieId=1`
- POST `/api/shows`
- GET `/api/shows/{showId}/seats`
- POST `/api/bookings`
- GET `/api/bookings/{bookingId}`
- POST `/api/bookings/{bookingId}/cancel`

Booking JSON:
```json
{"customerName":"Arun","customerEmail":"arun@example.com","showId":1,"seatIds":[1,2]}
```

## Important
This is a learning MVP, not production ticketing software. Payment is simulated; no card/UPI details are collected. Admin APIs are not yet protected by login/roles. Add Spring Security before public deployment. Booking is transactional and uses row locking plus a unique constraint to prevent double booking.

## Subject mapping
Java: APIs and business logic. DBMS: schema, keys, joins, transactions. FDS: Pandas and Matplotlib analytics. Software Engineering: project plan, requirements and test cases in `documentation/`.
