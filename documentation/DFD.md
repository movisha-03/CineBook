CineBook – Data Flow Diagram (DFD)

1. Introduction

A Data Flow Diagram (DFD) represents how data moves between users, processes and databases in the CineBook application.

2. Level 0 DFD – Context Diagram

```mermaid
flowchart LR
    U[User] -->|Movie Selection and Booking Details| S((CineBook System))
    S -->|Movie Details and Booking Confirmation| U
    A[Admin] -->|Movie and Show Details| S
    S -->|Analytics Reports| A
    S <-->|Booking and Payment Records| D[(MySQL Database)]
```

3. Level 1 DFD

```mermaid
flowchart TD
    U[User]

    P1((1.0 View Movies))
    P2((2.0 View Shows))
    P3((3.0 Book Tickets))
    P4((4.0 Process Demo Payment))
    P5((5.0 Generate Analytics))

    D1[(Movies and Shows)]
    D2[(Bookings)]
    D3[(Payments)]

    U --> P1
    P1 <--> D1
    P1 --> U

    U --> P2
    P2 <--> D1
    P2 --> U

    U --> P3
    P3 --> D2
    P3 --> P4

    P4 --> D3
    P4 --> U

    D2 --> P5
    D3 --> P5
    P5 --> R[Analytics Reports]
```

4. Data Stores

- **D1 – Movies and Shows:** Stores movie, screen and show details.
- **D2 – Bookings:** Stores ticket booking information.
- **D3 – Payments:** Stores demo payment information.

5. Data Flow Description

1. User requests available movies.
2. CineBook retrieves movie details from MySQL.
3. User selects a movie and show.
4. Booking details are processed and stored.
5. Demo payment information is recorded.
6. Python analytics processes booking and payment data.
7. Reports and charts are generated.

6. Conclusion

The DFD explains the flow of information between the user, CineBook processes and MySQL database. It also shows how booking data is used for analytics.