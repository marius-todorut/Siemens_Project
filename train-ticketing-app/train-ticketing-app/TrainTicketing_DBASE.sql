CREATE DATABASE TrainTicketing_DBASE;
GO

USE TrainTicketing_DBASE;
GO

---------------------------------------------------
-- TABLE: trains
---------------------------------------------------

CREATE TABLE trains (
     id INT IDENTITY(1,1) PRIMARY KEY,train_name NVARCHAR(100) NOT NULL UNIQUE,total_seats INT NOT NULL
     CHECK (total_seats > 0)
);

---------------------------------------------------
-- TABLE: routes
---------------------------------------------------

CREATE TABLE routes (
    id INT IDENTITY(1,1) PRIMARY KEY,

    route_name NVARCHAR(100) NOT NULL UNIQUE
);

---------------------------------------------------
-- TABLE: route_stations
---------------------------------------------------

CREATE TABLE route_stations (
    id INT IDENTITY(1,1) PRIMARY KEY,

    route_id INT NOT NULL,

    station_name NVARCHAR(100) NOT NULL,

    station_order INT NOT NULL
        CHECK (station_order > 0),

    CONSTRAINT fk_route_stations_routes
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_route_station_order
        UNIQUE(route_id, station_order)
);

---------------------------------------------------
-- TABLE: train_schedules
---------------------------------------------------

CREATE TABLE train_schedules (
    id INT IDENTITY(1,1) PRIMARY KEY,

    train_id INT NOT NULL,

    route_id INT NOT NULL,

    departure_time DATETIME2 NOT NULL,

    arrival_time DATETIME2 NOT NULL,

    delay_minutes INT NOT NULL
        DEFAULT 0
        CHECK (delay_minutes >= 0),

    CONSTRAINT fk_schedules_trains
        FOREIGN KEY (train_id)
        REFERENCES trains(id),

    CONSTRAINT fk_schedules_routes
        FOREIGN KEY (route_id)
        REFERENCES routes(id),

    CONSTRAINT chk_schedule_times
        CHECK (arrival_time > departure_time)
);

---------------------------------------------------
-- TABLE: bookings
---------------------------------------------------

CREATE TABLE bookings (
    id INT IDENTITY(1,1) PRIMARY KEY,

    customer_name NVARCHAR(100) NOT NULL,

    customer_email NVARCHAR(150) NOT NULL,

    schedule_id INT NOT NULL,

    tickets_count INT NOT NULL
        CHECK (tickets_count > 0),

    booking_time DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    CONSTRAINT fk_bookings_schedules
        FOREIGN KEY (schedule_id)
        REFERENCES train_schedules(id)
);

---------------------------------------------------
-- INDEXES
---------------------------------------------------

CREATE INDEX idx_bookings_schedule
ON bookings(schedule_id);

CREATE INDEX idx_route_stations_route
ON route_stations(route_id);

CREATE INDEX idx_train_schedules_route
ON train_schedules(route_id);

CREATE INDEX idx_train_schedules_train
ON train_schedules(train_id);

---------------------------------------------------
-- SAMPLE DATA
---------------------------------------------------

---------------------------------------------------
-- TRAINS
---------------------------------------------------

INSERT INTO trains(train_name, total_seats)
VALUES
    ('IR 1745', 120),
    ('IR 1832', 150),
    ('RE 2104', 80),
    ('IC 532', 100);

---------------------------------------------------
-- ROUTES
---------------------------------------------------

INSERT INTO routes(route_name)
VALUES
    ('Cluj - Bucuresti'),
    ('Cluj - Timisoara'),
    ('Brasov - Constanta'),
    ('Sibiu - Bucuresti');

---------------------------------------------------
-- ROUTE STATIONS
---------------------------------------------------

-- Route 1

INSERT INTO route_stations(route_id, station_name, station_order)
VALUES
    (1, 'Cluj-Napoca', 1),
    (1, 'Alba Iulia', 2),
    (1, 'Sibiu', 3),
    (1, 'Brasov', 4),
    (1, 'Bucuresti', 5);

-- Route 2

INSERT INTO route_stations(route_id, station_name, station_order)
VALUES
    (2, 'Cluj-Napoca', 1),
    (2, 'Oradea', 2),
    (2, 'Arad', 3),
    (2, 'Timisoara', 4);

-- Route 3

INSERT INTO route_stations(route_id, station_name, station_order)
VALUES
    (3, 'Brasov', 1),
    (3, 'Ploiesti', 2),
    (3, 'Bucuresti', 3),
    (3, 'Constanta', 4);

-- Route 4

INSERT INTO route_stations(route_id, station_name, station_order)
VALUES
    (4, 'Sibiu', 1),
    (4, 'Pitesti', 2),
    (4, 'Bucuresti', 3);

---------------------------------------------------
-- TRAIN SCHEDULES
---------------------------------------------------

INSERT INTO train_schedules(
    train_id,
    route_id,
    departure_time,
    arrival_time,
    delay_minutes
)
VALUES
(
    1,
    1,
    '2026-06-01 08:00:00',
    '2026-06-01 16:00:00',
    0
),
(
    2,
    2,
    '2026-06-01 09:30:00',
    '2026-06-01 15:30:00',
    0
),
(
    3,
    3,
    '2026-06-01 17:00:00',
    '2026-06-01 22:00:00',
    0
),
(
    4,
    4,
    '2026-06-01 12:00:00',
    '2026-06-01 17:30:00',
    0
);

---------------------------------------------------
-- SAMPLE BOOKINGS
---------------------------------------------------

INSERT INTO bookings(
    customer_name,
    customer_email,
    schedule_id,
    tickets_count
)
VALUES
(
    'Marius Todorut',
    'ticketappsiemens@gmail.com',
    1,
    2
),
(
    'Alex Pop',
    'ticketappsiemens@gmail.com',
    1,
    1
),
(
    'Ioana Ionescu',
    'ticketappsiemens@gmail.com',
    2,
    3
);

GO

INSERT INTO routes(route_name)
VALUES
    ('Bucuresti - Constanta');

GO

INSERT INTO route_stations(
    route_id,
    station_name,
    station_order
)
VALUES
    (5, 'Bucuresti', 1),
    (5, 'Constanta', 2);

GO

INSERT INTO train_schedules(
    train_id,
    route_id,
    departure_time,
    arrival_time,
    delay_minutes
)
VALUES
(
    3,
    5,
    '2026-06-01 18:00:00',
    '2026-06-01 21:00:00',
    0
);

GO

SELECT * FROM trains;
GO

SELECT * FROM routes;
GO

SELECT * FROM route_stations;
GO

SELECT * FROM train_schedules;
GO

SELECT * FROM bookings;
GO