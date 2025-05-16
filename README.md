#  Billing System (JDBC + MySQL)

This is a simple billing system implemented in Java using JDBC and MySQL. It simulates a billing counter used by two types of users:

- **Customer**
- **Cashier**

Both users interact with the application through a terminal-based interface.

---

##  Project Structure

The system uses a MySQL database with predefined schema and data. The Java application connects to this database using the JDBC driver.

---

##  Setup Instructions

### 1. Create the MySQL Database

Before running the app, set up the database by executing the following SQL files in order:

1. `create_store.sql`  
2. `alter_store.sql`  
3. `insert_store.sql`

In your MySQL terminal, run:

```sql
SOURCE /path/to/create_store.sql;
SOURCE /path/to/alter_store.sql;
SOURCE /path/to/insert_store.sql;
```

## Compile and Run the Application

Ensure you have the JDBC connector JAR file (e.g. mysql-connector-j-8.0.32.jar) available.

Set the CLASSPATH
Linux/macOS:

```bash
export CLASSPATH='/path/to/mysql-connector-j-8.0.32.jar:.'
```
Now you are all set to run the application :
```bash
javac App.java
java App
```
