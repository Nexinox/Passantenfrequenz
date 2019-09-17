# Passantenfrequenz

# Installation

Requirements:
Wildfly 11-Final
Java 8
PostgresSQL

Download all files from latest Release \n
in pgadmin wich comes packaged with postgresql create a database named overlay\n
execute sql code found in data.sql as query in pgadmin\n
create a rsc folder in wildfly/bin folder and copy the provided databaseConfig.txt file into it and edit it as needed (fill in PostgresSQL username and password)\n
put the war file into wildfly/standalone/deployments folder\n
\n
add a admin and a normal user using wildfly/bin/add-user.bat\n
open add-user.bat\n
press b to create Application User \n
set a name and password\n
set groups\n
the admin user has to have groups "admins, users"\n
and the user only "users"\n
repeat until wanted number of users exist\n
\n
start wildfly and open localhost:8080/"name of war file minus the .war" log in as admin user and make needed configurations \n
put .xls tabels into wildfly/bin/rsc/cls folder that will read it into the database and back it up\n
\n\n
# Usage \n
login as normal user/ admin select time and hit start \n

