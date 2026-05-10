package ro.marius.train;

public class AdminService {

    private final MailService mailService =new MailService();

    public void addTrain(String trainName,int totalSeats){
        if(trainName== null ||trainName.isBlank()){
            System.out.println(" Train name cannot be empty! ");
            return;
        }
        if(totalSeats<= 0) {
            System.out.println(" Seats must be positive!");
            return;
        }
        var sql="""
                INSERT INTO trains(train_name, total_seats)
                VALUES (?, ?)
                """;

        try(var con=Database.getConnection();
            var stmt = con.prepareStatement(sql)) {
            stmt.setString(1,trainName);
            stmt.setInt(2, totalSeats);
            stmt.executeUpdate();

            System.out.println(" Train added successfully! ");

        }catch (Exception e) {
            System.out.println("Could not add train: " +e.getMessage()
            );
        }
    }

    public void updateTrain(int trainId,String trainName,int totalSeats) {
        var sql="""
                UPDATE trains
                SET train_name = ?, total_seats = ?
                WHERE id = ?
                """;

        var checkSql= """
                SELECT ISNULL(SUM(b.tickets_count), 0) AS booked_seats
                FROM train_schedules ts
                LEFT JOIN bookings b
                    ON b.schedule_id = ts.id
                WHERE ts.train_id = ?
                """;

        try(var con=Database.getConnection()) {
            try(var checkStmt=con.prepareStatement(checkSql)) {
                checkStmt.setInt(1, trainId);
                try(var rs=checkStmt.executeQuery()) {
                    rs.next();
                    var bookedSeats =rs.getInt("booked_seats");
                    if(totalSeats< bookedSeats) {
                        System.out.println("Total seats cannot be smaller than booked seats (" + bookedSeats + ").");
                        return;
                    }
                }
            }

            try(var stmt =con.prepareStatement(sql)) {
                stmt.setString(1, trainName);
                stmt.setInt(2, totalSeats);
                stmt.setInt(3, trainId);

                var rows= stmt.executeUpdate();

                if(rows==0) {
                    System.out.println("Train not found.");

                }else {
                    System.out.println("Train updated successfully.");
                }
            }

        }catch(Exception e) {
            System.out.println("Could not update train: " + e.getMessage());
        }
    }

    public void removeTrain(int trainId) {

        var sql="DELETE FROM trains WHERE id = ?";

        try(var con=Database.getConnection();
             var stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, trainId);

            var rows= stmt.executeUpdate();

            if(rows== 0) {

                System.out.println("Train not found.");

            }else{
                System.out.println("Train removed successfully.");
            }

        } catch(Exception e) {

            System.out.println("Could not remove train. " + "It may be used by schedules.");
        }
    }

    public void addRoute(String routeName,String stationsLine) {

        var routeSql= """
                INSERT INTO routes(route_name)
                VALUES (?)
                """;

        var stationSql= """
                INSERT INTO route_stations(
                    route_id,
                    station_name,
                    station_order
                )
                VALUES (?, ?, ?)
                """;

        try(var con= Database.getConnection()) {

            con.setAutoCommit(false);

            try(var routeStmt = con.prepareStatement(routeSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                routeStmt.setString(1, routeName);

                routeStmt.executeUpdate();

                try(var keys=routeStmt.getGeneratedKeys()) {

                    keys.next();

                    var routeId =keys.getInt(1);

                    var stations =stationsLine.split(",");

                    var uniqueStations =new java.util.HashSet<String>();

                    for(var station :stations) {

                        var cleanStation=station.trim().toLowerCase();

                        if(cleanStation.isBlank()) {

                            System.out.println("Station name cannot be empty.");
                            con.rollback();
                            return;
                        }

                        if (!uniqueStations.add(cleanStation)) {

                            System.out.println("Duplicate stations are not allowed.");
                            con.rollback();

                            return;
                        }
                    }

                    try(var stationStmt =con.prepareStatement(stationSql)) {

                        for (var i = 0;i < stations.length; i++) {

                            stationStmt.setInt(1,routeId);
                            stationStmt.setString(2, stations[i].trim());
                            stationStmt.setInt(3, i + 1);
                            stationStmt.addBatch();
                        }

                        stationStmt.executeBatch();
                    }
                }

                con.commit();

                System.out.println("Route added successfully.");

            }catch(Exception e) {

                con.rollback();

                System.out.println("Could not add route: " +e.getMessage());
            }

        } catch(Exception e) {

            System.out.println("Database error: " +e.getMessage());
        }
    }

    public void removeRoute(int routeId) {

        var sql= "DELETE FROM routes WHERE id = ?";

        try(var con= Database.getConnection();
             var stmt =con.prepareStatement(sql)) {

            stmt.setInt(1, routeId);

            var rows = stmt.executeUpdate();

            if(rows == 0) {

                System.out.println("Route not found.");

            }else{

                System.out.println("Route removed successfully.");
            }

        }catch(Exception e) {

            System.out.println("Could not remove route. " + "It may be used by schedules.");
        }
    }
    public void updateRoute(int routeId,String newRouteName,String stationsLine) {

        var updateRouteSql = """
            UPDATE routes
            SET route_name = ?
            WHERE id = ?
            """;

        var deleteStationsSql = """
            DELETE FROM route_stations
            WHERE route_id = ?
            """;

        var insertStationSql = """
            INSERT INTO route_stations(
                route_id,
                station_name,
                station_order
            )
            VALUES (?, ?, ?)
            """;

        try(var con =Database.getConnection()) {

            con.setAutoCommit(false);

            var checkSql = """
                SELECT COUNT(*)
                FROM train_schedules
                WHERE route_id = ?
                """;

            try(var checkStmt =con.prepareStatement(checkSql)) {

                checkStmt.setInt(1, routeId);

                try(var rs =checkStmt.executeQuery()) {
                    rs.next();
                    if(rs.getInt(1) > 0) {
                        System.out.println("Cannot update route because it is already used by schedules.");
                        con.rollback();
                        return;
                    }
                }
            }

            try {
                try (var updateStmt =con.prepareStatement(updateRouteSql)) {
                    updateStmt.setString(1, newRouteName);
                    updateStmt.setInt(2, routeId);
                    var rows = updateStmt.executeUpdate();
                    if (rows == 0) {
                        System.out.println("Route not found.");
                        con.rollback();
                        return;
                    }
                }

                try(var deleteStmt =con.prepareStatement(deleteStationsSql)) {
                    deleteStmt.setInt(1,routeId);
                    deleteStmt.executeUpdate();
                }

                var stations = stationsLine.split(",");
                var uniqueStations =new java.util.HashSet<String>();

                for(var station : stations) {
                    var cleanStation =station.trim().toLowerCase();
                    if(cleanStation.isBlank()) {
                        System.out.println("Station name cannot be empty.");
                        con.rollback();
                        return;
                    }
                    if (!uniqueStations.add(cleanStation)) {
                        System.out.println("Duplicate stations are not allowed.");
                        con.rollback();
                        return;
                    }
                }
                try(var insertStmt =con.prepareStatement(insertStationSql)) {
                    for(var i = 0;i < stations.length;i++) {

                        insertStmt.setInt(1, routeId);
                        insertStmt.setString(2, stations[i].trim());
                        insertStmt.setInt(3, i + 1);
                        insertStmt.addBatch();
                    }

                    insertStmt.executeBatch();
                }

                con.commit();

                System.out.println("Route updated successfully.");

            } catch(Exception e) {
                con.rollback();
                System.out.println("Could not update route: " + e.getMessage());
            }

        }catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void showBookingsForSchedule(int scheduleId) {

        var scheduleSql = """
                SELECT
                    ts.id,
                    t.train_name,
                    r.route_name,
                    ts.departure_time,
                    ts.arrival_time,
                    ts.delay_minutes
                FROM train_schedules ts
                JOIN trains t
                    ON ts.train_id = t.id
                JOIN routes r
                    ON ts.route_id = r.id
                WHERE ts.id = ?
                """;

        var bookingsSql = """
                SELECT
                    id,
                    customer_name,
                    customer_email,
                    tickets_count,
                    booking_time
                FROM bookings
                WHERE schedule_id = ?
                ORDER BY booking_time
                """;

        try (var con=Database.getConnection()) {

            try(var scheduleStmt =con.prepareStatement(scheduleSql)) {
                scheduleStmt.setInt(1, scheduleId);
                try(var scheduleRs =scheduleStmt.executeQuery()) {

                    if(!scheduleRs.next()) {
                        System.out.println("Schedule not found.");
                        return;
                    }

                    System.out.println("\n========== Schedule Details ==========");
                    System.out.println("Schedule ID: " + scheduleRs.getInt("id"));
                    System.out.println("Train: " + scheduleRs.getString("train_name"));
                    System.out.println("Route: " + scheduleRs.getString("route_name"));
                    System.out.println("Departure: " + scheduleRs.getTimestamp("departure_time"));
                    System.out.println("Arrival: " + scheduleRs.getTimestamp("arrival_time"));
                    System.out.println("Delay: " + scheduleRs.getInt("delay_minutes") + " minutes");
                }
            }

            try(var bookingStmt =con.prepareStatement(bookingsSql)) {

                bookingStmt.setInt(1, scheduleId);
                try(var rs= bookingStmt.executeQuery()) {

                    var found=false;
                    var totalTickets =0;
                    System.out.println("\n========== Bookings ==========");
                    while(rs.next()) {

                        found = true;
                        var tickets =rs.getInt("tickets_count");
                        totalTickets+= tickets;
                        System.out.println("--------------------------------------");
                        System.out.println("Booking ID: " + rs.getInt("id"));
                        System.out.println("Customer: " +rs.getString("customer_name"));
                        System.out.println("Email: " + rs.getString("customer_email"));
                        System.out.println("Tickets: " + tickets);
                        System.out.println("Booking time: " + rs.getTimestamp("booking_time"));
                    }

                    if (!found) {
                        System.out.println("No bookings found for this schedule.");

                    } else {
                        System.out.println("--------------------------------------");
                        System.out.println("Total booked tickets: " + totalTickets);
                    }
                }
            }

        }catch(Exception e) {
            System.out.println("Could not show bookings: " + e.getMessage());
        }
    }
    public void setDelay(int scheduleId,int delayMinutes) {

        var updateSql = """
            UPDATE train_schedules
            SET delay_minutes = ?
            WHERE id = ?
            """;

        var customersSql ="""
            SELECT DISTINCT
                b.customer_name,
                b.customer_email,
                t.train_name,
                r.route_name,
                ts.departure_time
            FROM bookings b
            JOIN train_schedules ts
                ON b.schedule_id = ts.id
            JOIN trains t
                ON ts.train_id = t.id
            JOIN routes r
                ON ts.route_id = r.id
            WHERE ts.id = ?
            """;

        try(var con= Database.getConnection();
             var updateStmt =con.prepareStatement(updateSql)) {

            updateStmt.setInt(1, delayMinutes);
            updateStmt.setInt(2, scheduleId);

            var rows=updateStmt.executeUpdate();

            if (rows== 0) {

                System.out.println("Schedule not found.");

                return;
            }

            try(var customerStmt = con.prepareStatement(customersSql)) {

                customerStmt.setInt(1, scheduleId);

                try(var rs= customerStmt.executeQuery()) {
                    while (rs.next()) {

                        var text = """
                            Hello %s,

                            We inform you that your train has a delay.

                            Train: %s
                            Route: %s
                            Initial departure: %s
                            Delay: %d minutes

                            We apologize for the inconvenience.
                            """.formatted(rs.getString("customer_name"),
                                rs.getString("train_name"),
                                rs.getString("route_name"),
                                rs.getTimestamp("departure_time"),
                                delayMinutes);

                        mailService.sendMail(rs.getString("customer_email"), "Train delay notification", text);
                    }
                }
            }
            System.out.println("Delay updated and customers notified!!");

        } catch(Exception e) {
            System.out.println("Could not set delay: " + e.getMessage());
        }
    }
}