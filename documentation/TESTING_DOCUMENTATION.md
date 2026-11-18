CINEBOOK – TESTING DOCUMENTATION

1. Introduction

Testing is performed to verify that the CineBook Movie Ticket Booking System works correctly. It helps identify errors and ensures that the application meets its requirements.

2. Testing Objectives

- Verify that the application starts successfully.
- Check movie and show details.
- Verify the ticket booking process.
- Check demo payment functionality.
- Verify database connectivity and data storage.
- Check Python-based box office analytics.

3. Test Cases

| Test Case ID | Test Scenario | Expected Result | Status |
|---|---|---|---|
| TC01 | Start Spring Boot application | Application starts successfully | To be verified |
| TC02 | View movie list | Movie details are displayed | To be verified |
| TC03 | View available shows | Show details are displayed | To be verified |
| TC04 | Book a movie ticket | Booking is confirmed | To be verified |
| TC05 | Complete demo payment | Demo payment success message is displayed | To be verified |
| TC06 | Check MySQL database | Booking and payment records are stored | To be verified |
| TC07 | Run Python analytics | Analytics report is generated | To be verified |
| TC08 | Generate analytics charts | Charts are saved successfully | To be verified |

4. Testing Methods

4.1 Unit Testing
Individual components of the application are checked separately.

4.2 Integration Testing
The frontend, Spring Boot backend, MySQL database and Python analytics are checked together.

4.3 System Testing
The complete CineBook application is tested to verify its overall functionality.

4.4 Database Testing
SQL queries are used to verify movie, show, booking and payment records.

5. Tools Used

- Visual Studio Code
- Java and Spring Boot
- MySQL Workbench
- Python
- Pandas
- Matplotlib
- Web Browser

6. Conclusion

Testing helps ensure that the CineBook application performs its main operations correctly. Further testing is required to verify all test cases and improve the reliability of the system.