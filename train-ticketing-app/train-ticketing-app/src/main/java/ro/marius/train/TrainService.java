package ro.marius.train;

import java.sql.Connection;

public class TrainService {

    private final MailService mailService=new MailService();

    public void showAllSchedules() {

        var sql = """
                SELECT 
                    ts.id,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time,
                    t.total_seats,
                    ISNULL(SUM(b.tickets_count), 0) AS booked_seats,
                    ts.delay_minutes
                FROM train_schedules ts
                JOIN trains t ON ts.train_id = t.id
                JOIN routes r ON ts.route_id = r.id
                LEFT JOIN bookings b ON b.schedule_id = ts.id
                GROUP BY 
                    ts.id,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time,
                    t.total_seats,
                    ts.delay_minutes
                ORDER BY ts.departure_time
                """;

        try(var con=Database.getConnection();
             var stmt=con.prepareStatement(sql);
             var rs=stmt.executeQuery()) {

            var found=false;

            System.out.println("\nAvailable train schedules:");

            while(rs.next()) {

                found=true;

                var freeSeats = rs.getInt("total_seats") - rs.getInt("booked_seats");

                System.out.println("--------------------------------------");
                System.out.println("Schedule ID: " + rs.getInt("id"));
                System.out.println("Train: " + rs.getString("train_name"));
                System.out.println("Route: " + rs.getString("route_name"));
                System.out.println("Departure: " + rs.getTimestamp("departure_time"));
                System.out.println("Arrival: " + rs.getTimestamp("arrival_time"));
                System.out.println("Free seats: " + freeSeats);
                System.out.println("Delay: " + rs.getInt("delay_minutes") + " minutes");
            }

            if(!found) {
                System.out.println("No schedules found.");
            }

        }catch(Exception e) {
            System.out.println("Could not load schedules: " + e.getMessage()
            );
        }
    }

    public void bookTickets(String customerName, String customerEmail,int scheduleId, int ticketsCount
    ) {

        if(customerName== null || customerName.isBlank()) {
            System.out.println("Customer name cannot be empty.");
            return;
        }

        if(customerEmail== null || customerEmail.isBlank()) {
            System.out.println("Customer email cannot be empty.");
            return;
        }

        if(ticketsCount <=0) {
            System.out.println("Tickets count must be positive.");
            return;
        }

        var seatsSql= """
                SELECT 
                    t.total_seats,
                    ISNULL(SUM(b.tickets_count), 0) AS booked_seats,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time
                FROM train_schedules ts WITH (UPDLOCK)
                JOIN trains t ON ts.train_id = t.id
                JOIN routes r ON ts.route_id = r.id
                LEFT JOIN bookings b ON b.schedule_id = ts.id
                WHERE ts.id = ?
                GROUP BY 
                    t.total_seats,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time
                """;

        var insertSql ="""
                INSERT INTO bookings(
                    customer_name,
                    customer_email,
                    schedule_id,
                    tickets_count
                )
                VALUES (?, ?, ?, ?)
                """;

        try(var con=Database.getConnection()) {

            con.setAutoCommit(false);


            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try(var seatsStmt=con.prepareStatement(seatsSql)) {

                seatsStmt.setInt(1,scheduleId);

                try(var rs=seatsStmt.executeQuery()) {

                    if(!rs.next()) {

                        System.out.println("Schedule not found.");
                        con.rollback();

                        return;
                    }

                    var totalSeats=rs.getInt("total_seats");

                    var bookedSeats=rs.getInt("booked_seats");

                    var freeSeats=totalSeats - bookedSeats;

                    if(ticketsCount>freeSeats) {

                        System.out.println("Not enough free seats. Available seats: " + freeSeats);

                        con.rollback();

                        return;
                    }

                    try(var insertStmt=con.prepareStatement(insertSql)) {

                        insertStmt.setString(1, customerName);
                        insertStmt.setString(2, customerEmail);
                        insertStmt.setInt(3, scheduleId);
                        insertStmt.setInt(4, ticketsCount);

                        insertStmt.executeUpdate();
                    }

                    con.commit();

                    var text = """
                            Hello %s,

                            Your train booking was confirmed.

                            Train: %s
                            Route: %s
                            Departure: %s
                            Arrival: %s
                            Tickets: %d

                            Thank you!
                            """.formatted(
                            customerName,
                            rs.getString("train_name"),
                            rs.getString("route_name"),
                            rs.getTimestamp("departure_time"),
                            rs.getTimestamp("arrival_time"),
                            ticketsCount
                    );

                    mailService.sendMail(customerEmail, "Train booking confirmation", text);

                    System.out.println("Booking completed successfully.");
                }

            } catch(Exception e) {

                con.rollback();

                System.out.println("Booking failed: " + e.getMessage());
            }

        } catch(Exception e) {

            System.out.println("Database error: " + e.getMessage());
        }
    }

    public boolean findDirectRoutes(String departureStation,String arrivalStation) {

        var sql ="""
                SELECT DISTINCT
                    ts.id,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time
                FROM train_schedules ts
                JOIN trains t ON ts.train_id = t.id
                JOIN routes r ON ts.route_id = r.id
                JOIN route_stations s1 ON s1.route_id = r.id
                JOIN route_stations s2 ON s2.route_id = r.id
                WHERE LOWER(s1.station_name) = LOWER(?)
                  AND LOWER(s2.station_name) = LOWER(?)
                  AND s1.station_order < s2.station_order
                ORDER BY ts.departure_time
                """;

        try (var con=Database.getConnection();
             var stmt=con.prepareStatement(sql)) {

            stmt.setString(1, departureStation);
            stmt.setString(2, arrivalStation);

            try(var rs= stmt.executeQuery()) {

                var found =false;

                while(rs.next()) {

                    if(!found) {
                        System.out.println("\nDirect routes:");
                    }

                    found =true;

                    System.out.println("--------------------------------------");
                    System.out.println("Schedule ID: " + rs.getInt("id"));
                    System.out.println("Train: " + rs.getString("train_name"));
                    System.out.println("Route: " + rs.getString("route_name"));
                    System.out.println("Departure: " + rs.getTimestamp("departure_time"));
                    System.out.println("Arrival: " + rs.getTimestamp("arrival_time"));
                }

                return found;
            }

        }catch(Exception e) {

            System.out.println("Search failed: " + e.getMessage());
        }

        return false;
    }

    public boolean findRoutesWithOneChange(String departureStation, String arrivalStation) {

        var sql = """
        SELECT DISTINCT
            ts1.id AS first_schedule_id,
            t1.train_name AS first_train,
            r1.route_name AS first_route,
            ts1.departure_time AS first_departure,
            ts1.arrival_time AS first_arrival,

            middle.station_name AS change_station,

            ts2.id AS second_schedule_id,
            t2.train_name AS second_train,
            r2.route_name AS second_route,
            ts2.departure_time AS second_departure,
            ts2.arrival_time AS second_arrival

        FROM train_schedules ts1

        JOIN trains t1
            ON ts1.train_id = t1.id

        JOIN routes r1
            ON ts1.route_id = r1.id

        JOIN route_stations start_station
            ON start_station.route_id = r1.id

        JOIN route_stations middle
            ON middle.route_id = r1.id

        JOIN route_stations middle2
            ON LOWER(middle.station_name) =
               LOWER(middle2.station_name)

        JOIN routes r2
            ON r2.id = middle2.route_id

        JOIN train_schedules ts2
            ON ts2.route_id = r2.id

        JOIN trains t2
            ON ts2.train_id = t2.id

        JOIN route_stations end_station
            ON end_station.route_id = r2.id

        WHERE LOWER(start_station.station_name) = LOWER(?)

          AND LOWER(end_station.station_name) = LOWER(?)

          AND start_station.station_order
              < middle.station_order

          AND middle2.station_order
              < end_station.station_order

          AND middle2.station_order = 1

          AND ts1.arrival_time <= ts2.departure_time

          AND ts1.id <> ts2.id

        ORDER BY ts1.departure_time,
                 ts2.departure_time
        """;

        try(var con=Database.getConnection();
             var stmt=con.prepareStatement(sql)) {

            stmt.setString(1, departureStation);
            stmt.setString(2, arrivalStation);

            try(var rs=stmt.executeQuery()) {

                var found= false;

                while(rs.next()) {

                    if (!found) {

                        System.out.println("\nPossible routes with one train change:");
                    }

                    found =true;

                    System.out.println("\n======================================");

                    System.out.println("STEP 1");

                    System.out.println("--------------------------------------");

                    System.out.println("Train: " + rs.getString("first_train"));

                    System.out.println("Route: " + rs.getString("first_route"));

                    System.out.println("Schedule ID: " + rs.getInt("first_schedule_id"));

                    System.out.println("Departure: " + rs.getTimestamp("first_departure"));

                    System.out.println("Arrival: " + rs.getTimestamp("first_arrival"));

                    System.out.println("\nChange train at: " + rs.getString("change_station"));

                    System.out.println("\nSTEP 2");

                    System.out.println("--------------------------------------");

                    System.out.println("Train: " + rs.getString("second_train"));

                    System.out.println("Route: " + rs.getString("second_route"));

                    System.out.println("Schedule ID: " + rs.getInt("second_schedule_id"));

                    System.out.println("Departure: " + rs.getTimestamp("second_departure"));

                    System.out.println("Arrival: " + rs.getTimestamp("second_arrival"));
                }

                return found;
            }

        } catch(Exception e) {

            System.out.println("Search failed: " + e.getMessage());
        }

        return false;
    }

    public void findRoutes(String departureStation,String arrivalStation) {

        var directFound=findDirectRoutes(departureStation, arrivalStation);

        if(!directFound){

            var changeFound=findRoutesWithOneChange(departureStation, arrivalStation);

            if(!changeFound) {

                System.out.println("No routes found between these stations.");
            }
        }
    }
}