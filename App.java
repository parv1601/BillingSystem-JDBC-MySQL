import java.sql.*;
import java.util.Scanner;

public class App {

    static final String DB_URL = "jdbc:mysql://localhost:3306/billing";
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String USER = "root";
    static final String PASSWORD = "Parv1601#$%";

    public static void main(String[] args) throws Exception {
        Connection conn = null;
        conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        conn.setAutoCommit(false);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n***********************************");
            System.out.println("          BILLING APP");
            System.out.println("***********************************");
            System.out.println("1. ENTER AS A CASHIER");
            System.out.println("2. ENTER AS A CUSTOMER");
            System.out.println("3. EXIT");
            System.out.print("SELECT ROLE (1-3): ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    System.out.println("\n--- CASHIER LOGIN ---");
                    Cashier cashier = new Cashier(conn);
                    cashier.cashierMenu();
                    break;

                case 2:
                    System.out.println("\n--- CUSTOMER LOGIN ---");
                    Customer customer = new Customer(conn);
                    if (customer.login()) {
                        customer.customerMenu();
                    } else {
                        System.out.println("Login failed. Please try again.");
                    }
                    break;

                case 3:
                    conn.close();
                    System.out.println("\nThank you for using the Billing App. Goodbye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("\nINVALID CHOICE! Please select a valid option (1-3).");
                    break;
            }
        }
    }
}
