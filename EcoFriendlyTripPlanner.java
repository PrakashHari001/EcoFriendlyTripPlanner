package p1;
import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

abstract class TransportMode {
    protected String name;
    protected double emissionFactor; // CO2 in grams per km
    protected double speed; // km/h

    public TransportMode(String name, double emissionFactor, double speed) {
        this.name = name;
        this.emissionFactor = emissionFactor;
        this.speed = speed;
    }

    public String getName() { return name; }
    public double getEmissionFactor() { return emissionFactor; }
    public abstract double calculateCarbonFootprint(double distance);
    public abstract double estimateTravelTime(double distance);
}

class Walk extends TransportMode {
    public Walk() { super("Walk", 0.0, 5.0); }

    @Override
    public double calculateCarbonFootprint(double distance) { return 0.0; }

    @Override
    public double estimateTravelTime(double distance) { return distance / speed; }
}

class Bike extends TransportMode {
    public Bike() { super("Bike", 0.0, 15.0); }

    @Override
    public double calculateCarbonFootprint(double distance) { return 0.0; }

    @Override
    public double estimateTravelTime(double distance) { return distance / speed; }
}

class Bus extends TransportMode {
    public Bus() { super("Bus", 50.0, 30.0); }

    @Override
    public double calculateCarbonFootprint(double distance) { return distance * emissionFactor; }

    @Override
    public double estimateTravelTime(double distance) { return distance / speed; }
}

class Trip {
    private String start;
    private String destination;
    private double distance;
    private TransportMode mode;
    private String date;

    public Trip(String start, String destination, double distance, TransportMode mode, String date) {
        this.start = start;
        this.destination = destination;
        this.distance = distance;
        this.mode = mode;
        this.date = date;
    }

    public double getCarbonFootprint() { return mode.calculateCarbonFootprint(distance); }

    public double getTravelTime() { return mode.estimateTravelTime(distance); }

    public String getSummary() {
        return String.format("%s: %s to %s, %.1f km, %s, %.1f g CO2, %.2f hours",
                date, start, destination, distance, mode.getName(), getCarbonFootprint(), getTravelTime());
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter("trip_history.txt", true)) {
            writer.write(getSummary() + "\n");
        } catch (IOException e) {
            System.out.println("Error saving trip: " + e.getMessage());
        }
    }

    public TransportMode getMode() { return mode; }
}

public class EcoFriendlyTripPlanner {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<TransportMode> modes = Arrays.asList(new Walk(), new Bike(), new Bus());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (true) {
            System.out.println("\n=== Eco-Friendly Trip Planner ===");
            System.out.println("1. Plan a new trip");
            System.out.println("2. View trip history");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                continue;
            }

            if (choice == 3) {
                System.out.println("Thank you for using Eco-Friendly Trip Planner!");
                scanner.close();
                break;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter start location: ");
                    String start = scanner.nextLine().trim();
                    if (start.isEmpty()) {
                        System.out.println("Start location cannot be empty!");
                        continue;
                    }

                    System.out.print("Enter destination: ");
                    String destination = scanner.nextLine().trim();
                    if (destination.isEmpty()) {
                        System.out.println("Destination cannot be empty!");
                        continue;
                    }

                    System.out.print("Enter distance (km): ");
                    double distance;
                    try {
                        distance = scanner.nextDouble();
                        if (distance <= 0) throw new IllegalArgumentException("Distance must be positive.");
                    } catch (InputMismatchException | IllegalArgumentException e) {
                        System.out.println("Invalid distance! Please enter a positive number.");
                        scanner.nextLine();
                        continue;
                    }
                    scanner.nextLine();

                    System.out.println("Available transport modes:");
                    for (int i = 0; i < modes.size(); i++) {
                        System.out.println((i + 1) + ". " + modes.get(i).getName());
                    }
                    System.out.print("Choose transport mode (1-" + modes.size() + "): ");
                    int modeChoice;
                    try {
                    	modeChoice = scanner.nextInt();
                        if (modeChoice < 1 || modeChoice > modes.size()) {
                            System.out.println("Invalid mode selection!");
                            scanner.nextLine();
                            continue;
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input! Please enter a number.");
                        scanner.nextLine();
                        continue;
                    }
                    scanner.nextLine();

                    String date = LocalDate.now().format(dateFormatter);

                    Trip trip = new Trip(start, destination, distance, modes.get(modeChoice - 1), date);
                    System.out.println("\nTrip Summary:\n" + trip.getSummary());
                    trip.saveToFile();
                    System.out.println("Trip saved to history!");

                    // Recommend the most eco-friendly mode
                    TransportMode bestMode = modes.stream()
                            .min(Comparator.comparing(m -> m.calculateCarbonFootprint(distance)))
                            .orElse(modes.get(0));
                    if (!bestMode.getName().equals(trip.getMode().getName())) {
                        System.out.printf("Recommendation: Use %s for a lower carbon footprint (%.1f g CO2)%n",
                                bestMode.getName(), bestMode.calculateCarbonFootprint(distance));
                    }
                    break;

                case 2:
                    try (BufferedReader reader = new BufferedReader(new FileReader("trip_history.txt"))) {
                        System.out.println("\n=== Trip History ===");
                        String line;
                        boolean hasHistory = false;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            hasHistory = true;
                        }
                        if (!hasHistory) {
                            System.out.println("No trip history found!");
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading trip history: " + e.getMessage());
                    }
                    break;

                default:
                    System.out.println("Invalid option! Please choose 1, 2, or 3.");
            }
        }
    }
}
