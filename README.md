OK!

# Train Ticketing Application

## Overview

This project is a console-based Train Ticketing Application developed in Java using JDBC, Microsoft SQL Server, Maven, and JavaMail.

The application manages train schedules, train routes, bookings, and customer notifications.Customers can search for routes, book tickets, and receive confirmation emails, while administrators can manage trains, routes,schedules, and delays.

The project was implemented using:

* **Java 25**
* **JDBC**
* **Microsoft SQL Server**
* **JavaMail (Jakarta Mail)**
* **Maven**
* **IntelliJ IDEA**

The application communicates directly with a SQL Server database and uses a real Gmail account for sending booking confirmations and delay notifications.
---

# Technologies and Libraries

## Programming Language

* Java 25

## Database

* Microsoft SQL Server

## Database Connectivity

* JDBC Driver for SQL Server

## Email Service

* Jakarta Mail / JavaMail

## Build Tool

* Maven

## IDE

* IntelliJ IDEA

---
# Project Structure

```text
train-ticketing-app
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── ro.marius.train
│   │   │       ├── Main.java
│   │   │       ├── ConsoleMenu.java
│   │   │       ├── TrainService.java
│   │   │       ├── AdminService.java
│   │   │       ├── MailService.java
│   │   │       └── Database.java
│   │   │
│   │   └── resources
│   │       └── config.properties
│
├── pom.xml
└── README.md
```

---
# Database Configuration

The database connection is configured inside the `config.properties` file.

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=TrainTicketing_DBASE;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=admin123
```
The application also contains Gmail SMTP configuration for sending emails.

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=ticketappsiemens@gmail.com
mail.password=gbet qmuj zrph yaoc
```

---

# Database Structure

The SQL Server database contains the following tables:

* `trains`
* `routes`
* `route_stations`
* `train_schedules`
* `bookings`

The database also includes:

* primary keys
* foreign keys
* indexes
* constraints
* sample data

---

# Application Functionalities

## 1. Showing Train Schedules

The application can display all available train schedules together with:

* train name
* route
* departure time
* arrival time
* free seats
* delays

Example output:

```text
Available train schedules:
--------------------------------------
Schedule ID: 1
Train: IR 1745
Route: Cluj - Bucuresti
Departure: 2026-06-01 08:00:00.0
Arrival: 2026-06-01 16:00:00.0
Free seats: 117
Delay: 0 minutes
```

The application automatically calculates the number of free seats by subtracting booked tickets from the total train capacity.

---

## 2. Booking Tickets

Customers can book one or multiple tickets for a train schedule.

During the booking process, the application:

* validates the input
* checks seat availability
* prevents overbooking
* stores the booking in SQL Server
* sends a confirmation email

Successful booking example:

```text
Customer name: Marius Todorut
Customer email: ticketappsiemens@gmail.com
Schedule ID: 1
Tickets count: 2

Email sent to ticketappsiemens@gmail.com
Booking completed successfully.
```

After the booking is completed, the customer receives a real email generated through Gmail SMTP.

<img width="912" height="909" alt="image" src="https://github.com/user-attachments/assets/1516ccc5-4a73-4288-9db7-456ede9c64b1" />

The email contains:

* train name
* route
* departure time
* arrival time
* number of booked tickets

---

The application also prevents overbooking.

Example:

```text
Customer name: Alex Test
Customer email: alex@gmail.com
Schedule ID: 1
Tickets count: 500

Not enough free seats. Available seats: 115
```

This validation ensures that bookings cannot exceed the available train capacity.

---

## 3. Finding Routes Between Stations

The system can search for direct train routes between two stations.

Example:

```text
Departure station: Cluj-Napoca
Arrival station: Bucuresti

Direct routes:
--------------------------------------
Schedule ID: 1
Train: IR 1745
Route: Cluj - Bucuresti
Departure: 2026-06-01 08:00:00.0
Arrival: 2026-06-01 16:00:00.0
```

If no valid route exists, the application displays an appropriate message.

Example:

```text
Departure station: Timisoara
Arrival station: Constanta

No routes found between these stations.
```

---

# Administrator Functionalities

The administrator menu allows full management of trains, routes, bookings, and delays.

---

## 4. Adding Trains

The administrator can add new trains together with their seat capacity.

Example:

```text
Train name: IR 999
Total seats: 200

Train added successfully!
```

After insertion, the train appears inside the SQL Server database.

<img width="540" height="761" alt="image" src="https://github.com/user-attachments/assets/c523e629-5f5f-4bce-92e5-d37bb6e9e813" />

This screenshot demonstrates:

* existing trains
* updated trains
* added routes
* stored data inside SQL Server

---

## 5. Updating Trains

Train information can also be updated.

Example:

```text
Train ID: 1
New train name: IR Updated
New total seats: 140

Train updated successfully.
```

If the train does not exist, the application handles the error correctly.

Example:

```text
Train ID: 999
New train name: Ghost Train
New total seats: 100

Train not found.
```

---

## 6. Removing Trains

The administrator can remove trains that are not referenced by schedules.

Example:

```text
Train ID: 5

Train removed successfully.
```

If the train is already used by a schedule, SQL Server foreign key protection prevents deletion.

Example:

```text
Train ID: 1

Could not remove train. It may be used by schedules.
```

---

## 7. Adding Routes

The administrator can create routes containing multiple stations.

Example:

```text
Route name: Cluj - Iasi
Stations separated by comma: Cluj-Napoca,Targu Mures,Bacau,Iasi

Route added successfully.
```

The application validates duplicate stations.

Example:

```text
Route name: Bad Route
Stations separated by comma: Cluj-Napoca,Cluj-Napoca

Duplicate stations are not allowed.
```

---

## 8. Updating Routes

Routes can be updated together with their station list.

Example:

```text
Route ID: 6
New route name: Updated Route
New stations separated by comma: Cluj-Napoca,Suceava,Iasi

Route updated successfully.
```

Routes already used by schedules cannot be modified.

Example:

```text
Route ID: 1
New route name: Blocked Route
New stations separated by comma: A,B,C

Cannot update route because it is already used by schedules.
```

---

## 9. Removing Routes

Unused routes can be deleted successfully.

Example:

```text
Route ID: 6

Route removed successfully.
```

Routes already connected to schedules are protected by database constraints.

Example:

```text
Route ID: 1

Could not remove route. It may be used by schedules.
```

---

## 10. Viewing Schedule Bookings

The administrator can inspect all bookings for a train schedule.

Example:

```text
========== Schedule Details ==========
Schedule ID: 1
Train: IR Updated
Route: Cluj - Bucuresti
Departure: 2026-06-01 08:00:00.0
Arrival: 2026-06-01 16:00:00.0
Delay: 0 minutes
```

The application also displays:

* booking IDs
* customer names
* customer emails
* number of booked tickets
* booking timestamps

Example:

```text
Booking ID: 1
Customer: Marius Todorut
Email: ticketappsiemens@gmail.com
Tickets: 2
```

<img width="1044" height="760" alt="image" src="https://github.com/user-attachments/assets/2cef0243-e5bc-4147-ad4d-1e37fea996bb" />

This figure demonstrates:

* persisted bookings
* customer data
* booked ticket counts
* booking timestamps

---

If the schedule does not exist, the application displays an error message.

```text
Schedule ID: 999

Schedule not found.
```

---

## 11. Setting Train Delays

The administrator can update train delays.

Example:

```text
Schedule ID: 1
Delay minutes: 45

Email sent to ticketappsiemens@gmail.com
Email sent to ticketappsiemens@gmail.com

Delay updated and customers notified!!
```

When a delay is set:

* the database is updated
* all affected customers receive email notifications automatically

<img width="1600" height="104" alt="image" src="https://github.com/user-attachments/assets/5aa128da-46d2-4104-befb-d139f6b65749" />

This figure demonstrates:

* automatically generated delay notification emails
* real Gmail SMTP integration
* customer notification functionality

---

If the schedule does not exist, the application displays:

```text
Schedule ID: 999
Delay minutes: 15

Schedule not found.
```

---

# SQL Server Results

The database content can be verified directly inside Microsoft SQL Server Management Studio.

<img width="958" height="798" alt="image" src="https://github.com/user-attachments/assets/ab06b067-835c-4633-82ed-ad8152da06cf" />

This figure demonstrates:

* route station ordering
* train schedules
* departure and arrival times
* delay values stored in the database

---

# Validation and Error Handling

The application contains multiple validation mechanisms:

* empty input validation
* positive number validation
* email validation
* duplicate station validation
* overbooking prevention
* foreign key protection
* transaction rollback support

---

# Concurrency Protection

The booking system uses:

* transactions
* SERIALIZABLE isolation level
* SQL Server row locking

This prevents multiple users from booking more seats than available at the same time.

---



