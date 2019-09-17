# Passantenfrequenz

# Installation

Requirements:
Wildfly 11-Final
Java 8
PostgresSQL

-Download all files from latest Release
-in pgadmin wich comes packaged with postgresql create a database named overlay
-execute sql code found in data.sql as query in pgadmin
-create a rsc folder in wildfly/bin folder and copy the provided databaseConfig.txt file into it and edit it as needed (fill in PostgresSQL username and password)
-put the war file into wildfly/standalone/deployments folder

-add a admin and a normal user using wildfly/bin/add-user.bat
open add-user.bat
press b to create Application User 
set a name and password
set groups
the admin user has to have groups "admins, users"
and the user only "users"
repeat until wanted number of users exist

-start wildfly and open localhost:8080/"name of war file minus the .war" log in as admin user and make needed configurations 
-put .xls tabels into wildfly/bin/rsc/cls folder that will read it into the database and back it up

# Usage 
login as normal user/ admin select time and hit start 
