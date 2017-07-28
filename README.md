TRADEMETRICS - calculation of network metris on CITES bird trade data

This project allows to calculate network metrics for the trade database of birds prepared from the CITES Trade Database. 
It uses two tables as source data from a mysql database, one with the network vertices, and another with network edges, to calculate several network metrics, using the Gephi Toolkit Library (https://gephi.org/toolkit).
It returns to the database a table with all metrics deteremined for each bird species. As an option, it allows the output of a printout as pdf of each network image.

Requirements
1. MySQL server
2. Java JRE version 1.7 or superior

Installation
1. Create a mysql database
> mysqladmin -u <username> -p create <database>
2. In mysql, create database user and provide password and appropriate credentials
3. Import the database schema template, and fill it with data
> mysql -u <username> -p <database> <- database_template.sql
3. Run the application, after compiling it with Netbeans
