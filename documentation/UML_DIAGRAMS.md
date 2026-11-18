CineBook – UML Diagrams

1. Introduction

UML (Unified Modeling Language) diagrams represent the structure and behaviour of the CineBook system.

2. Use Case Diagram

```mermaid
flowchart LR
    User((User))
    Admin((Admin))
    User --> A[View Movies]
    User --> B[View Shows]
    User --> C[Book Tickets]
    User --> D[Make Demo Payment]
    User --> E[View Booking Confirmation]
    Admin --> F[Manage Movies]
    Admin --> G[Manage Shows]
```

3. Class Diagram

```mermaid
classDiagram
    class Movie {
        int movieId
        String title
        String genre
    }

    class Show {
        int showId
        String showDate
        String showTime
        double ticketPrice
    }

    class Booking {
        int bookingId
        int userId
        double totalAmount
        String status
    }

    class Payment {
        int paymentId
        double amount
        String paymentStatus
    }

    class Screen {
        int screenId
        String screenName
        int capacity
    }

    Movie "1" --> "*" Show
    Screen "1" --> "*" Show
    Show "1" --> "*" Booking
    Booking "1" --> "1" Payment
```

4. Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Backend
    participant Database

    User->>Frontend: Select Movie and Show
    Frontend->>Backend: Request Show Details
    Backend->>Database: Fetch Show Details
    Database-->>Backend: Return Show Details
    Backend-->>Frontend: Display Available Shows
    User->>Frontend: Confirm Booking
    Frontend->>Backend: Submit Booking
    Backend->>Database: Save Booking
    Database-->>Backend: Booking Saved
    Backend-->>Frontend: Booking Confirmation
    Frontend-->>User: Display Confirmation
```

5. Activity Diagram

```mermaid
flowchart TD
    A([Start]) --> B[Open CineBook]
    B --> C[View Movies]
    C --> D[Select Movie]
    D --> E[Select Show]
    E --> F[Select Seats]
    F --> G[Confirm Booking]
    G --> H[Demo Payment]
    H --> I[Booking Confirmation]
    I --> J([End])
```

6. Conclusion

The UML diagrams describe the use cases, classes, interactions and activities of the CineBook application.