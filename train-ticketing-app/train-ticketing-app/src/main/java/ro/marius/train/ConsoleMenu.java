package ro.marius.train;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner=new Scanner(System.in);

    private final TrainService trainService =new TrainService();

    private final AdminService adminService =new AdminService();


    public void start() {
        while (true) {
            System.out.println("""
                    
                            Train Ticketing Application 
                    1. Show all train schedules
                    2. Book tickets
                    3. Find routes between stations
                    4. Admin menu
                    0. Exit
                    Choose option:
                    """);

            var option=scanner.nextLine();

            switch(option) {
                case "1"-> trainService.showAllSchedules();
                case "2"-> bookTickets();
                case "3"-> findRoutes();
                case "4"-> adminMenu();
                case "0"-> {System.out.println("Application closed.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void bookTickets() {
        var name=readRequiredText("Customer name: ");
        var email=readEmail("Customer email: ");
        var scheduleId=readPositiveInt("Schedule ID: ");
        var ticketsCount=readPositiveInt("Tickets count: ");

        trainService.bookTickets(name,email,scheduleId,ticketsCount);
    }

    private void findRoutes() {
        var departure= readRequiredText("Departure station: ");
        var arrival =readRequiredText("Arrival station: ");
        if(departure.equalsIgnoreCase(arrival)) {
            System.out.println("Departure and arrival stations cannot be the same.");
            return;
        }

        trainService.findRoutes(departure,arrival);
    }

    private void adminMenu() {
        while (true) {
            System.out.println("""
                    
                    ===== Admin Menu =====
                    1. Add train
                    2. Update train
                    3. Remove train
                    4. Add route
                    5. Update route
                    6. Remove route
                    7. Show bookings for schedule
                    8. Set train delay
                    0. Back
                    Choose option:
                    """);

            var option=scanner.nextLine();

            switch (option) {
                case "1"-> addTrain();
                case "2"-> updateTrain();
                case "3" -> removeTrain();
                case "4"-> addRoute();
                case "5" -> updateRoute();
                case "6"->removeRoute();
                case "7"-> showBookings();
                case "8" ->setDelay();
                case "0" ->{
                    return;
                }
                default->System.out.println("Invalid option.");
            }
        }
    }

    private void addTrain() {
        var name=readRequiredText("Train name: ");

        var seats=readPositiveInt("Total seats: ");

        adminService.addTrain(name, seats);
    }

    private void updateTrain() {
        var id=readPositiveInt("Train ID: ");

        var name=readRequiredText("New train name: ");

        var seats=readPositiveInt("New total seats: ");

        adminService.updateTrain(id,name,seats);
    }

    private void removeTrain() {
        var id=readPositiveInt("Train ID: ");

        adminService.removeTrain(id);
    }

    private void addRoute() {
        var routeName=readRequiredText("Route name: ");

        System.out.print("Stations separated by comma: ");
        var stations= scanner.nextLine().trim();

        if(stations.isBlank()) {
            System.out.println("Stations cannot be empty.");
            return;
        }

        var splitStations=stations.split(",");

        if(splitStations.length< 2) {
            System.out.println("A route must contain at least 2 stations.");
            return;
        }

        adminService.addRoute(routeName,stations);
    }
    private void updateRoute() {
        var routeId=readPositiveInt("Route ID: ");

        var routeName=readRequiredText("New route name: ");

        System.out.print("New stations separated by comma: ");
        var stations=scanner.nextLine().trim();

        if(stations.isBlank()) {
            System.out.println("Stations cannot be empty.");
            return;
        }

        var splitStations=stations.split(",");

        if(splitStations.length < 2) {
            System.out.println("A route must contain at least 2 stations.");
            return;
        }

        adminService.updateRoute(routeId,routeName,stations);
    }

    private void removeRoute() {
        var id=readPositiveInt("Route ID: ");

        adminService.removeRoute(id);
    }

    private void showBookings() {
        var scheduleId=readPositiveInt("Schedule ID: ");

        adminService.showBookingsForSchedule(scheduleId);
    }

    private void setDelay() {
        var scheduleId=readPositiveInt("Schedule ID: ");

        var delay=readPositiveInt("Delay minutes: ");

        adminService.setDelay(scheduleId, delay);
    }
    private int readPositiveInt(String message) {
        while(true) {
            try {
                System.out.print(message);

                var value=Integer.parseInt(scanner.nextLine());

                if(value<= 0) {
                    System.out.println("Value must be positive.");
                    continue;
                }

                return value;

            }catch(Exception e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readRequiredText(String message) {
        while(true) {
            System.out.print(message);

            var text=scanner.nextLine().trim();

            if(text.isBlank()) {
                System.out.println("Value cannot be empty.");
                continue;
            }

            return text;
        }
    }

    private String readEmail(String message) {
        while(true) {
            System.out.print(message);

            var email=scanner.nextLine().trim();

            if(email.isBlank()) {
                System.out.println("Email cannot be empty!!");
                continue;
            }

            if(!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                System.out.println("Invalid email format.");
                continue;
            }
            return email;
        }
    }
}