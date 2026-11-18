# CineBook – Software Engineering Project Plan

## Problem statement
Manual theatre scheduling and ticket records can cause booking conflicts, seat availability errors and limited insight into occupancy and revenue. CineBook provides a web workflow for movie/show management, seat booking, cancellation and box-office analysis.

## Objectives
1. Maintain movie, screen, seat and show information.
2. Display show-wise seat availability.
3. Prevent duplicate seat bookings.
4. Record bookings, demo payments, cancellations and refunds.
5. Analyze movie popularity, revenue and occupancy using Python.

## Actors
- Customer: browse movies, choose shows/seats, book and cancel.
- Admin: add movies and schedule shows.
- Analyst: run Python reports from database data.

## Functional requirements
Movie/show listing; seat inventory; customer booking; cancellation; demo payment record; revenue and occupancy analytics.

## Non-functional requirements
Input validation; transactional booking consistency; secure database credentials; responsive UI; useful error messages.

## Diagrams to prepare
Use Case Diagram, DFD Level 0 and 1, ER Diagram, UML Class Diagram, Sequence Diagram and Activity Diagram.

## Initial test cases
| ID | Test | Expected result |
|---|---|---|
| TC01 | Open movie list | Active movies returned |
| TC02 | Select valid show | Show seats returned |
| TC03 | Book available seat | Booking confirmed |
| TC04 | Book same seat again | Conflict response |
| TC05 | Cancel confirmed booking | Booking cancelled and seat released |
| TC06 | Request unknown booking | Not found response |
| TC07 | Add movie with blank title | Validation error |
| TC08 | Run Python analysis | Summary and charts generated |

## Limitations / next milestones
This starter uses simulated payments and does not yet include login security. Add authentication, admin role protection, cancellation policy and deployment before public use.
