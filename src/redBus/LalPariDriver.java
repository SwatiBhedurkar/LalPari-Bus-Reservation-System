package redBus;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/* ================= MANAGER ================= */
class Manager {
    private long contact = 9999999999L;
    private String password = "admin";

    public long getContact() { return contact; }
    public String getPassword() { return password; }
}

/* ================= PASSENGER ================= */
class Passenger {

    private String name;
    private String password;
    private long contact;
    private String gender;
    private String seatNumber;
    private int failedAttempts = 0;
    private boolean locked = false;

    public Passenger(String name, String password, long contact, String gender) {
        this.name = name;
        this.password = password;
        this.contact = contact;
        this.gender = gender;
    }

    public String getName() { return name; }
    public long getContact() { return contact; }
    public String getPassword() { return password; }
    public String getGender() { return gender; }
    public String getSeatNumber() { return seatNumber; }

    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public void incrementFail() {
        failedAttempts++;
        if (failedAttempts >= 3) locked = true;
    }

    public boolean isLocked() { return locked; }
    public void resetFail() { failedAttempts = 0; }
}

/* ================= BUS ================= */
class Bus {

    private final int CAPACITY = 40;
    private Passenger[] passengers = new Passenger[CAPACITY];
    private String[][] seats = new String[10][4];

    public Bus() {
        char row = 'A';
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 4; j++) {
                seats[i][j] = row + "" + (j + 1);
            }
            row++;
        }
    }

    public boolean isSeatExist(String seat) {
        for (String[] row : seats)
            for (String s : row)
                if (s.equalsIgnoreCase(seat))
                    return true;
        return false;
    }

    public boolean isSeatAvailable(String seat) {
        for (Passenger p : passengers)
            if (p != null && seat.equalsIgnoreCase(p.getSeatNumber()))
                return false;
        return true;
    }

    public void showSeatLayout() {
        System.out.println("\n===== BUS SEAT LAYOUT =====");
        for (String[] row : seats) {
            for (String seat : row) {
                if (isSeatAvailable(seat))
                    System.out.print("[" + seat + "]");
                else
                    System.out.print("[XX]");
            }
            System.out.println();
        }
    }
    /* ---------- Seat Statistics ---------- */

    public int getTotalSeats() {
        return CAPACITY;
    }

    public int getBookedSeatsCount() {
        int count = 0;
        for (Passenger p : passengers) {
            if (p != null) count++;
        }
        return count;
    }

    public int getAvailableSeatsCount() {
        return CAPACITY - getBookedSeatsCount();
    }

    public void showSeatStatistics() {
        System.out.println("\n========= BUS STATISTICS =========");
        System.out.println("Total Seats       : " + getTotalSeats());
        System.out.println("Booked Seats      : " + getBookedSeatsCount());
        System.out.println("Available Seats   : " + getAvailableSeatsCount());
        System.out.println("Occupancy Rate    : " +
                (getBookedSeatsCount() * 100 / CAPACITY) + "%");
        System.out.println("==================================\n");
    }

    /* ---------- Dynamic Pricing ---------- */
    public double calculateDynamicPrice(String seat) {
        double price = 1500;
        if (seat.endsWith("1") || seat.endsWith("4"))
            price += 100;
        return price;
    }

    /* ---------- Booking ---------- */
    public void bookSeat(Passenger p, String seat) {

        for (int i = 0; i < CAPACITY; i++) {
            if (passengers[i] == null) {

                double price = calculateDynamicPrice(seat);

                passengers[i] = p;
                p.setSeatNumber(seat);

                saveBookingToFile(p.getName(), p.getContact(), seat, price);

                System.out.println("Booking Successful!");
                System.out.println("Seat: " + seat);
                System.out.println("Price: ₹" + price);
                return;
            }
        }
        System.out.println("Bus Full!");
    }

    /* ---------- Save Booking ---------- */
    public void saveBookingToFile(String name, long contact, String seat, double price) {
        try {
            FileWriter fw = new FileWriter("booking_history.txt", true);
            fw.write("========================================\n");
            fw.write("Booking Date : " + LocalDate.now() + "\n");
            fw.write("Passenger    : " + name + "\n");
            fw.write("Contact      : " + contact + "\n");
            fw.write("Seat Number  : " + seat + "\n");
            fw.write("Amount Paid  : ₹" + price + "\n");
            fw.write("========================================\n");
            fw.close();
        } catch (Exception e) {
            System.out.println("Error saving booking.");
        }
    }

    /* ---------- ROLE BASED HISTORY ---------- */
    public void viewBookingHistory(long loggedInContact, boolean isManager) {

        try {
            BufferedReader br = new BufferedReader(new FileReader("booking_history.txt"));
            String line;
            boolean showBlock = false;
            boolean found = false;

            System.out.println("\n========= BOOKING HISTORY =========\n");

            while ((line = br.readLine()) != null) {

                if (line.contains("Contact      : " + loggedInContact)) {
                    showBlock = true;
                    found = true;
                }

                if (isManager) {
                    System.out.println(line);
                }
                else if (showBlock) {
                    System.out.println(line);
                }

                if (line.contains("========================================")) {
                    showBlock = false;
                }
            }

            if (!found && !isManager) {
                System.out.println("No booking history found.");
            }

            br.close();

        } catch (Exception e) {
            System.out.println("No booking history available.");
        }
    }
}

/* ================= APPLICATION ================= */
class Application {

    private Bus bus = new Bus();
    private ArrayList<Passenger> users = new ArrayList<>();
    private Manager manager = new Manager();
    private Scanner sc = new Scanner(System.in);

    public void start() {

        while (true) {
            System.out.println("\n1.Register\n2.Login\n3.Exit");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> System.exit(0);
            }
        }
    }

    private void register() {

        System.out.print("Name: ");
        String name = sc.next();

        System.out.print("Contact: ");
        long contact = sc.nextLong();

        System.out.print("Password: ");
        String pass = sc.next();

        System.out.print("Gender (M/F): ");
        String gender = sc.next();

        users.add(new Passenger(name, pass, contact, gender));
        System.out.println("Registered Successfully!");
    }

    private void login() {

        System.out.print("Contact: ");
        long contact = sc.nextLong();

        System.out.print("Password: ");
        String pass = sc.next();

        /* ----- Manager Login ----- */
        if (contact == manager.getContact() &&
                pass.equals(manager.getPassword())) {

            System.out.println("Manager Logged In");
            managerMenu();
            return;
        }

        /* ----- Passenger Login ----- */
        for (Passenger p : users) {

            if (p.getContact() == contact) {

                if (p.isLocked()) {
                    System.out.println("Account Locked!");
                    return;
                }

                if (p.getPassword().equals(pass)) {
                    p.resetFail();
                    passengerMenu(p);
                    return;
                } else {
                    p.incrementFail();
                    System.out.println("Wrong Password!");
                    return;
                }
            }
        }

        System.out.println("User Not Found");
    }

    private void passengerMenu(Passenger p) {

        while (true) {

            System.out.println("\n1.Book\n2.View Seats\n3.View My Booking History\n4.Logout");
            int ch = sc.nextInt();

            switch (ch) {

                case 1 -> {
                    bus.showSeatLayout();
                    System.out.print("Enter Seat: ");
                    String seat = sc.next();

                    if (!bus.isSeatExist(seat))
                        System.out.println("Invalid Seat Number!");
                    else if (!bus.isSeatAvailable(seat))
                        System.out.println("Seat Not Available!");
                    else
                        bus.bookSeat(p, seat);
                }

                case 2 -> bus.showSeatLayout();

                case 3 -> bus.viewBookingHistory(p.getContact(), false);

                case 4 -> { return; }
            }
        }
    }

    private void managerMenu() {

        while (true) {

            System.out.println("\n===== MANAGER PANEL =====");
            System.out.println("1.View Seat Layout");
            System.out.println("2.View Booking History");
            System.out.println("3.View Seat Statistics");
            System.out.println("4.Logout");

            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> bus.showSeatLayout();
                case 2 -> bus.viewBookingHistory(0, true);  
                case 3 -> bus.showSeatStatistics();
                case 4 -> { return; }
            }
        }
    }
}

/* ================= MAIN ================= */
public class LalPariDriver {
    public static void main(String[] args) {
        new Application().start();
    }
}