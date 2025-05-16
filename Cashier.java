import java.sql.*;
import java.util.Scanner;
import java.math.BigDecimal;
import java.nio.channels.SelectableChannel;

public class Cashier {

    private Connection conn;

    public Cashier(Connection conn) {
        this.conn = conn;
    }

    public void cashierMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n***********************************");
            System.out.println("          CASHIER MENU");
            System.out.println("***********************************");
            System.out.println("1. SCAN ITEMS AND CREATE BILL");
            System.out.println("2. VIEW ALL PRODUCTS");
            System.out.println("3. EXIT TO MAIN MENU");
            System.out.print("SELECT OPTION (1-3): ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    scanItems();
                    break;

                case 2:
                    viewAllProducts();
                    break;

                case 3:
                    System.out.println("\nReturning to Main Menu...");
                    return;

                default:
                    System.out.println("\nINVALID CHOICE! Please select a valid option (1-4).");
                    break;
            }
        }
    }


    public void scanItems() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- SCAN ITEMS ---");
        System.out.println("Please enter the customer details to create a bill.");
        System.out.print("ENTER CUSTOMER NAME : ");
        String customer_name = scanner.nextLine();
        if(customer_name.isBlank()) customer_name = null;

        System.out.print("ENTER CUSTOMER PHONE : ");
        String customer_phone = scanner.nextLine();
        if(customer_phone.isBlank()) customer_phone = null;

        System.out.print("ENTER CUSTOMER EMAIL : ");
        String customer_email = scanner.nextLine();
        if(customer_email.isBlank()) customer_email = null;

        String query = "SELECT customer_id, name, email FROM customers WHERE phone = ?";
        int customer_id = -1;

        try {
            // Check if customer exists or create a new one
            PreparedStatement cust_st = conn.prepareStatement(query);
            cust_st.setString(1, customer_phone);
            ResultSet cust_rs = cust_st.executeQuery();

            if (cust_rs.next()) {
                if (!cust_rs.getString("name").equals(customer_name)) {
                    System.out.println("CUSTOMER NAME DOES NOT MATCH WITH THE PHONE NUMBER!");
                    return;
                }
                if (!cust_rs.getString("email").equals(customer_email)) {
                    System.out.println("CUSTOMER EMAIL DOES NOT MATCH WITH THE PHONE NUMBER!");
                    return;
                }
                customer_id = cust_rs.getInt("customer_id");
            } else {
                String insertQuery = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, customer_name);
                insertStmt.setString(2, customer_phone);
                insertStmt.setString(3, customer_email);
                int aff_rows = insertStmt.executeUpdate();
                if (aff_rows > 0) {
                    try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            customer_id = keys.getInt(1);
                        }
                    }
                }
            }

            // Create a new bill
            String query1 = "INSERT INTO bills (customer_id, date_time, total_amount, discount) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, customer_id);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(2, currentTimestamp);
            stmt.setBigDecimal(3, BigDecimal.ZERO);
            stmt.setBigDecimal(4, BigDecimal.ZERO);

            int bill_id = -1;
            int aff_rows = stmt.executeUpdate();
            if (aff_rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        bill_id = keys.getInt(1);
                    }
                }
            }

            BigDecimal total_amount = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;

            viewAllProducts();

            // Process items
            while (true) {
                System.out.print("ENTER PRODUCT ID OR -1 TO FINISH : ");
                String prd_id = scanner.nextLine();
                if(prd_id.isBlank()){
                    System.err.println("PRODCUT ID CAN NOT BE NULL");
                    try{
                        conn.rollback();
                    }
                    catch(SQLException se){
                        se.printStackTrace();
                    }
                    return ;
                }

                int product_id = Integer.parseInt(prd_id);
                if (product_id == -1) break;

                System.out.print("ENTER QUANTITY : ");
                String qt = scanner.nextLine();
                if(qt.isBlank()){
                    System.err.println("PLEASE ENTER QUANTITY!");
                    try{
                        conn.rollback();
                    }
                    catch(SQLException se){
                        se.printStackTrace();
                    }
                    return;
                }
                int qty = Integer.parseInt(qt);
                //scanner.nextLine();

                // Fetch product details
                String query2 = "SELECT price, stock_quantity FROM products WHERE product_id = ?";
                stmt = conn.prepareStatement(query2);
                stmt.setInt(1, product_id);
                ResultSet rs = stmt.executeQuery();

                BigDecimal price = BigDecimal.ZERO;
                int stock = 0;
                if (rs.next()) {
                    price = rs.getBigDecimal("price");
                    stock = rs.getInt("stock_quantity");
                } else {
                    System.out.println("PRODUCT ID NOT FOUND!");
                    continue;
                }

                if (stock < qty) {
                    System.out.println("STOCK NOT AVAILABLE! Available stock: " + stock);
                    continue;
                }

                rs.close();

                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));

                // Insert into bill_items
                String sql = "INSERT INTO bill_items (bill_id, product_id, quantity, item_total) " +
                             "VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), " +
                             "item_total = item_total + VALUES(item_total)";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, bill_id);
                stmt.setInt(2, product_id);
                stmt.setInt(3, qty);
                stmt.setBigDecimal(4, itemTotal);
                stmt.executeUpdate();

                // Update product stock
                String updateStockQuery = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
                stmt = conn.prepareStatement(updateStockQuery);
                stmt.setInt(1, qty);
                stmt.setInt(2, product_id);
                stmt.executeUpdate();

                // Fetch discount percentage
                String query4 = "SELECT dscnt_percentage FROM offers WHERE product_id = ?";
                stmt = conn.prepareStatement(query4);
                stmt.setInt(1, product_id);
                ResultSet rs1 = stmt.executeQuery();
                double dscnt = 0;
                while (rs1.next()) {
                    dscnt += rs1.getInt("dscnt_percentage");
                }
                rs1.close();

                dscnt = dscnt / 100;
                BigDecimal itemTotalDiscount = itemTotal.multiply(BigDecimal.valueOf(dscnt)); // Discounted item price

                total_amount = total_amount.add(itemTotal);
                discount = discount.add(itemTotalDiscount);
            }

            // Update the bill
            String updateQuery = "UPDATE bills SET total_amount = ?, discount = ? WHERE bill_id = ?";
            stmt = conn.prepareStatement(updateQuery);
            stmt.setBigDecimal(1, total_amount);
            stmt.setBigDecimal(2, discount);
            stmt.setInt(3, bill_id);
            stmt.executeUpdate();

            conn.commit(); // Commit the transaction

            // Print the bill
            System.out.println("\n--- BILL DETAILS ---");
            System.out.println("Customer Name: " + customer_name);
            System.out.println("Customer Phone: " + customer_phone);
            System.out.println("Total Amount: " + total_amount);
            System.out.println("Discount: " + discount);
            System.out.println("Payable Amount: " + total_amount.subtract(discount));
            System.out.println("Bill created successfully!");

        } catch (SQLException se) {
            try {
                conn.rollback(); // Rollback the transaction in case of an error
                System.out.println("Transaction rolled back due to an error.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            se.printStackTrace();
        }
    }

    public void viewAllProducts() {
        System.out.println("\n--- AVAILABLE PRODUCTS ---");
        try {
            String query = "SELECT product_id, name, price, stock_quantity FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.printf("%-10s %-20s %-10s %-10s\n", "ID", "NAME", "PRICE", "STOCK");
            System.out.println("---------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("price");
                int stock = rs.getInt("stock_quantity");

                System.out.printf("%-10d %-20s %-10.2f %-10d\n", id, name, price, stock);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("ERROR FETCHING PRODUCTS: " + e.getMessage());
        }
    }

}
