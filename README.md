# Train Ticketing Application

## Project Overview

This project is a console-based Train Ticketing Application developed in Java using JDBC, Microsoft SQL Server, and JavaMail.

The application allows customers to:view available train schedules,book one or multiple train tickets,search for routes between stations,receive booking confirmation emails

The application also provides an administrator menu with advanced functionalities such as:
- adding, updating, and removing trains
- adding, updating, and removing routes
- viewing bookings for a train schedule
- setting train delays and notifying customers by email

The application uses a real Gmail account configured through JavaMail in order to send real email notifications.

---

# Technologies Used

- Java 25
- JDBC
- Microsoft SQL Server
- JavaMail (Jakarta Mail)
- Maven
- IntelliJ IDEA
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
````

---

# Database Configuration

The application uses Microsoft SQL Server.

Database connection configuration:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=TrainTicketing_DBASE;encrypt=true;trustServerCertificate=true
db.user=your_sql_server_username
db.password=your_sql_server_password
```

---

# Email Configuration

The application sends real emails using Gmail SMTP.

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=your_email@gmail.com
mail.password=your_gmail_app_password
```
---

# Database Schema

The database contains the following tables:

* trains
* routes
* route_stations
* train_schedules
* bookings

The database also contains:

* primary keys
* foreign keys
* indexes
* constraints
* sample data

---

# Main Functionalities

## 1. Show All Train Schedules

The application displays all available train schedules together with:

* train name
* route
* departure time
* arrival time
* available seats
* delays

### Example Console Output

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

---

## 2. Ticket Booking

Customers can book one or multiple tickets.

The system:

* validates user input
* prevents overbooking
* stores bookings in the database
* sends confirmation emails

### Successful Booking Example

```text
Customer name: Marius Todorut
Customer email: ticketappsiemens@gmail.com
Schedule ID: 1
Tickets count: 2

Email sent to ticketappsiemens@gmail.com
Booking completed successfully.
```

### Overbooking Prevention Example

```text
Customer name: Alex Test
Customer email: alex@gmail.com
Schedule ID: 1
Tickets count: 500

Not enough free seats. Available seats: 115
```

---

## 3. Route Search

The application can search:

* direct routes
* routes with one train change

### Direct Route Example

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

### No Route Found Example

```text
Departure station: Timisoara
Arrival station: Constanta

No routes found between these stations.
```

---

# Administrator Functionalities

---

## 4. Add Train

### Example

```text
Train name: IR 999
Total seats: 200

Train added successfully!
```

---

## 5. Update Train

### Successful Update

```text
Train ID: 1
New train name: IR Updated
New total seats: 140

Train updated successfully.
```

### Train Not Found

```text
Train ID: 999
New train name: Ghost Train
New total seats: 100

Train not found.
```

---

## 6. Remove Train

### Train Used by Schedules

```text
Train ID: 1

Could not remove train. It may be used by schedules.
```

### Successful Removal

```text
Train ID: 5

Train removed successfully.
```

---

## 7. Add Route

### Successful Route Addition

```text
Route name: Cluj - Iasi
Stations separated by comma: Cluj-Napoca,Targu Mures,Bacau,Iasi

Route added successfully.
```

### Duplicate Stations Validation

```text
Route name: Bad Route
Stations separated by comma: Cluj-Napoca,Cluj-Napoca

Duplicate stations are not allowed.
```

---

## 8. Update Route

### Successful Update

```text
Route ID: 6
New route name: Updated Route
New stations separated by comma: Cluj-Napoca,Suceava,Iasi

Route updated successfully.
```

### Blocked Update

```text
Route ID: 1
New route name: Blocked Route
New stations separated by comma: A,B,C

Cannot update route because it is already used by schedules.
```

---

## 9. Remove Route

### Route Used by Schedule

```text
Route ID: 1

Could not remove route. It may be used by schedules.
```

### Successful Removal

```text
Route ID: 6

Route removed successfully.
```

---

## 10. Show Bookings for Schedule

### Successful Example

```text
========== Schedule Details ==========
Schedule ID: 1
Train: IR Updated
Route: Cluj - Bucuresti
Departure: 2026-06-01 08:00:00.0
Arrival: 2026-06-01 16:00:00.0
Delay: 0 minutes

========== Bookings ==========
--------------------------------------
Booking ID: 1
Customer: Marius Todorut
Email: ticketappsiemens@gmail.com
Tickets: 2
```

### Schedule Not Found

```text
Schedule ID: 999

Schedule not found.
```

---

## 11. Set Train Delay

The administrator can update train delays.

All customers that booked tickets for the train automatically receive email notifications.

### Successful Example

```text
Schedule ID: 1
Delay minutes: 45

Email sent to ticketappsiemens@gmail.com
Email sent to ticketappsiemens@gmail.com

Delay updated and customers notified!!
```

### Schedule Not Found

```text
Schedule ID: 999
Delay minutes: 15

Schedule not found.
```

---

# Database Results

The following screenshots show the database content after executing the application functionalities.

---

<img width="540" height="761" alt="image" src="https://github.com/user-attachments/assets/427af2eb-d2f7-4fb5-9a9b-474ddbc98ccf" />

This figure shows the trains and routes currently stored inside the SQL Server database after administrator operations such as updating trains and managing routes.

---

<img width="958" height="798" alt="image" src="https://github.com/user-attachments/assets/04ae22d1-78b7-4cd9-a100-e5b3ee47a9eb" />

This figure shows the route station ordering together with persisted train schedules and updated delay information stored in SQL Server.

---

<img width="1044" height="760" alt="image" src="https://github.com/user-attachments/assets/cddb8eca-b0a0-4e1e-9124-fa4e3349939b" />


This figure shows the persisted bookings stored in the database, including customer information, booked ticket counts, and booking timestamps.

---

# Email Notifications

The application uses a real Gmail account in order to send booking confirmations and train delay notifications.

---

<img width="1600" height="104" alt="image" src="https://github.com/user-attachments/assets/2100b71a-6c36-43ff-816b-6c7932b0088d" />



This figure shows the delay notification emails received by customers after the administrator updated the train delay inside the application.

<img width="912" height="909" alt="image" src="https://github.com/user-attachments/assets/f01bbda7-a71c-42f4-88b1-e85c941b1ce7" />

This figure shows the booking confirmation email automatically sent after a successful ticket reservation.

---

# Validation Rules

The application includes multiple validation mechanisms:
* empty text validation
* email format validation
* positive number validation
* duplicate station validation
* overbooking prevention
* foreign key protection
* route update restrictions
* train deletion restrictions
---

# Concurrency Protection

The booking system uses:

* transactions
* SERIALIZABLE isolation level
* SQL Server row locking

This prevents overbooking when multiple users try to book tickets simultaneously.


