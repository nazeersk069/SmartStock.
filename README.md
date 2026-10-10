# SmartStock - Inventory & Sales Management System

A console-based inventory and sales management application built with core Java, JDBC and SQL Server.

## Features
- User registration and login with BCrypt-hashed passwords
- Role-based access (ADMIN / STAFF)
- Product management: add, view, update, delete
- Sales module: selling a product automatically reduces stock (JDBC transaction)
- Low-stock alerts based on a reorder level
- Reports: low-stock report and CSV export of products and sales

## Tech Stack
Java 21, Maven, JDBC, Microsoft SQL Server, jBCrypt, VS Code

## How to run
1. Install JDK 21+, Maven and SQL Server Express.
2. Run database.sql in SSMS to create the database and tables.
3. Copy db.properties.example to db.properties (same folder) and enter your own SQL Server password.
4. In the terminal, go to backend/smartstock-backend and run: mvn compile exec:java "-Dexec.mainClass=com.smartstock.App"
5. Register a user, then make it admin in SSMS with: UPDATE Users SET role = 'ADMIN' WHERE username = 'yourname';
