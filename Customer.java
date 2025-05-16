import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.BigDecimal;

public class Customer {

    private Connection conn;
    private int customer_id;

    public Customer(Connection conn) {
        this.conn = conn;
    }

    public boolean login() {
        Scanner scanner = new Scanner(System.in);
        //System.out.println("\n--- CUSTOMER LOGIN ---");
        System.out.print("ENTER YOUR PHONE NUMBER: ");
        String phone = scanner.nextLine();

        System.out.print("ENTER YOUR EMAIL: ");
        String email = scanner.nextLine();

        System.out.print("ENTER YOUR NAME: ");
        String name = scanner.nextLine();

        String query = "SELECT customer_id FROM customers WHERE phone = ? AND email = ? AND name = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, phone);
            stmt.setString(2, email);
            stmt.setString(3, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                customer_id = rs.getInt("customer_id");
                System.out.println("Login successful! Welcome, " + name + "!");
                rs.close();
                stmt.close();
                return true;
            } else {
                System.out.println("ERROR: Incorrect phone number, email, or name. Please try again.");
                rs.close();
                stmt.close();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR OCCURRED DURING LOGIN: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void customerMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n***********************************");
            System.out.println("          CUSTOMER MENU");
            System.out.println("***********************************");
            System.out.println("1. MAKE PAYMENT");
            System.out.println("2. VIEW MY PURCHASES");
            System.out.println("3. VIEW PENDING BILLS");
            System.out.println("4. EXIT TO MAIN MENU");
            System.out.print("SELECT OPTION (1-4): ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    makePayment();
                    break;

                case 2:
                    viewMyPurchases();
                    break;

                case 3:
                    viewPendingBills();
                    break;

                case 4:
                    System.out.println("\nReturning to Main Menu...");
                    return;    

                default:
                    System.out.println("\nINVALID CHOICE! Please select a valid option (1-3).");
                    break;
            }
        }
    }


    public void viewMyPurchases() {
        System.out.println("\n--- YOUR PURCHASE HISTORY ---");

        try{
            // Fetch all bills for the customer
            String billsQuery = "SELECT bi.bill_id, bi.date_time, bi.total_amount, bi.discount FROM bills bi join payment p on bi.bill_id = p.bill_id WHERE bi.customer_id = ? and p.bill_id IS NOT NULL";
            PreparedStatement billsStmt = conn.prepareStatement(billsQuery);
            billsStmt.setInt(1, this.customer_id);
            ResultSet billsRs = billsStmt.executeQuery();

            System.out.println("\n--- BILLS ---");
            System.out.printf("%-10s %-20s %-15s %-15s\n", "BILL ID", "DATE", "TOTAL AMOUNT", "DISCOUNT");
            System.out.println("-------------------------------------------------------------");

            while (billsRs.next()) {
                int bill_id = billsRs.getInt("bill_id");
                Timestamp date = billsRs.getTimestamp("date_time");
                BigDecimal total_amount = billsRs.getBigDecimal("total_amount");
                BigDecimal discount = billsRs.getBigDecimal("discount");

                System.out.printf("%-10d %-20s %-15.2f %-15.2f\n", bill_id, date, total_amount, discount);

                // Fetch items for each bill
                String itemsQuery = "SELECT p.name, bi.quantity, bi.item_total " +
                                    "FROM bill_items bi " +
                                    "JOIN products p ON bi.product_id = p.product_id " +
                                    "WHERE bi.bill_id = ?";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);
                itemsStmt.setInt(1, bill_id);
                ResultSet itemsRs = itemsStmt.executeQuery();

                System.out.println("  Items:");
                System.out.printf("    %-20s %-10s %-10s\n", "PRODUCT NAME", "QUANTITY", "ITEM TOTAL");
                while (itemsRs.next()) {
                    String product_name = itemsRs.getString("name");
                    int quantity = itemsRs.getInt("quantity");
                    BigDecimal item_total = itemsRs.getBigDecimal("item_total");

                    System.out.printf("    %-20s %-10d %-10.2f\n", product_name, quantity, item_total);
                }
                itemsRs.close();
            }

            billsRs.close();


        }catch(SQLException se){
            se.printStackTrace();
        }

    }

    public void makePayment() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- MAKE PAYMENT ---");

        try {
            // Display pending bills
            viewPendingBills();

            // Select a bill to pay
            System.out.print("ENTER BILL ID TO PAY: ");
            int bill_id = scanner.nextInt();
            scanner.nextLine();

            // Fetch bill details
            String billQuery = "SELECT total_amount, discount FROM bills WHERE bill_id = ? AND customer_id = ?";
            PreparedStatement billStmt = conn.prepareStatement(billQuery);
            billStmt.setInt(1, bill_id);
            billStmt.setInt(2, customer_id);
            ResultSet billRs = billStmt.executeQuery();

            if (!billRs.next()) {
                System.out.println("ERROR: Invalid bill ID or bill does not belong to you.");
                return;
            }

            BigDecimal totalAmount = billRs.getBigDecimal("total_amount");
            BigDecimal discount = billRs.getBigDecimal("discount");
            BigDecimal payableAmount = totalAmount.subtract(discount);

            System.out.println("TOTAL AMOUNT: " + totalAmount);
            System.out.println("DISCOUNT: " + discount);
            System.out.println("PAYABLE AMOUNT: " + payableAmount);

            // Select payment method
            System.out.println("SELECT PAYMENT METHOD:");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. UPI");
            System.out.print("ENTER OPTION (1-3): ");
            int paymentOption = scanner.nextInt();
            scanner.nextLine();

            String paymentMethod;
            switch (paymentOption) {
                case 1:
                    paymentMethod = "Cash";
                    break;
                case 2:
                    paymentMethod = "Card";
                    break;
                case 3:
                    paymentMethod = "UPI";
                    break;
                default:
                    System.out.println("INVALID PAYMENT METHOD!");
                    return;
            }

            // Insert payment record
            String paymentQuery = "INSERT INTO payment (bill_id, customer_id, payment_mode) VALUES (?, ?, ?)";
            PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery);
            paymentStmt.setInt(1, bill_id);
            paymentStmt.setInt(2, customer_id);
            paymentStmt.setString(3, paymentMethod);
            //paymentStmt.setBigDecimal(4, payableAmount);

            paymentStmt.executeUpdate();
            conn.commit(); 

            System.out.println("Payment successful! Thank you for your payment.");
        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback transaction in case of error
                System.out.println("Transaction rolled back due to an error.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("ERROR PROCESSING PAYMENT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewPendingBills() {
        System.out.println("\n--- PENDING BILLS ---");

        try {
            String query = "SELECT b.bill_id, b.total_amount, b.discount, b.date_time " +
                           "FROM bills b " +
                           "LEFT JOIN payment p ON b.bill_id = p.bill_id " +
                           "WHERE b.customer_id = ? AND p.bill_id IS NULL";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customer_id);
            ResultSet rs = stmt.executeQuery();

            System.out.printf("%-10s %-15s %-10s %-20s\n", "BILL ID", "TOTAL AMOUNT", "DISCOUNT", "DATE");
            System.out.println("------------------------------------------------------------");
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                BigDecimal discount = rs.getBigDecimal("discount");
                Timestamp date = rs.getTimestamp("date_time");

                System.out.printf("%-10d %-15.2f %-10.2f %-20s\n", billId, totalAmount, discount, date);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("ERROR FETCHING PENDING BILLS: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
