# Passantenfrequenz

# Installation

Requirements:
Wildfly 11-Final -
Java 8 -
PostgresSQL

Download all files from latest Release. in pgadmin wich comes packaged with postgresql create a database named overlay. Execute sql code found in data.sql as query in pgadmin. Create a rsc folder in wildfly/bin folder and create a file named databaseConfig.txt and edit it, fill in PostgresSQL username and password in this order and in seperate lines. Put the war file into wildfly/standalone/deployments folder.

Add an admin and a normal user using wildfly/bin/add-user.bat. Open add-user.bat. Press b to create Application User. Set a name and password. Set groups. The admin user has to have groups "admins, users". And the user only "users". Repeat until wanted number of users exist.

Start wildfly and open localhost:8080/"name of war file minus the .war". Log in as admin user and make needed configurations. Put .xls tables into wildfly/bin/rsc/xls folder that will read it into the database and back it up.

# Usage 
login as normal user/ admin select time and hit start.

