# SIRS 2021/2022 project - Remote document access with ransomware protection
Group 52
- João Manuel Amaral Antunes - 102312
- David Miguel Franco Rodrigues - 102142
- Hélio Sven Figueiredo Borges - 93721

## Setup

#### Requirements

The following requirements are needed to run the program:
- Linux 64-bit, preferably Ubuntu 20.04
- Java 11
- Maven
- PostgreSQL 
- Apache ZooKeeper
- zkNaming, included in this repository


## Usage

This guide accounts for running the program localy. To run it in multiple VMs, some IP addresses in _pom.xml_ files need to be adjusted to your configuration of choice

For the program's execution, it'll require at least 5 terminal windows/VMs, so that the following modules can be launched:
- 1 CA Server
- 1 Leader Server
- 3 or more (always in an odd number) file servers
- 1 or more clients
- In case of VMs, both the database and ZooKeeper are launched in the same machine as the Leader Server

#### 0. Instalation

1. Initialize the ZooKeeper server by opening a terminal in the `zookeeper/bin` folder and executing:
```bash
./zkServer.sh start
```

2. Explaining from a fresh postgreSQL install, enter postgres with the following command:
```bash
sudo su - postgres
```
In here, we can create the database for the program with the following command:
```bash
createdb Ransom
```
But we have yet to initialize it. To do so, we now execute the following commands:
```bash
psql    #Connects to a default postgres database with a default user
\c Ransom   #Connects to the newly create database
\i <full-path-to-RansomwareResistantRemoteDocuments>/init.sql
# For an example of the command above: 
# \i /home/seed/RansomwareResistantRemoteDocuments/init.sql
\q  #Quit psql
```
We can now try to proceed with this guide, however we advise creating a new user to allow an easier connection to the database when executing the pograms, by using the default configuration. To do so, execute the following command:
```
createuser --interactive --pwprompt
```
When prompted, insert the username SIRS and password SIRS. No need to give root permissions. We can then quit postgres with:
```
exit
```

3. Open `RansomwareResistantRemoteDocuments/zkNaming` and run the following maven command:
```bash
mvn clean install
```

4. Now back in `RansomwareResistantRemoteDocuments` directory, you can run the following maven command to install the entire program:
```bash
mvn clean install
```

#### 1. CA Server

If you wish to run the module in its default configuration as set by its `pom.xml` file, simply run the following command in the `RansomwareResistantRemoteDocuments/CAServer` directory:
```bash
mvn compile exec:java
``` 

Alternatively, if you wish to set each parameter manually, you can also run the following command:

```bash
mvn compile exec:java -Dexec.mainClass="pt.tecnico.CAServer.CAServerMain" -Dexec.args="<zooHost> <zooPort> <serverPath> <serverHost> <serverPort> <keystorePass>"
```

- __zooHost__: The IP address of the ZooKeeper server
- __zooPort__: The port of the ZooKeeper server
- __serverPath__: The path name associated to this server, stored in the ZooKeeper server
- __serverHost__: The IP address of this server
- __serverPort__: The port of this server
- __keystorePass__: The password associated with this server's KeyStore instance


#### 2. Leader Server and File Servers

If you wish to run the module in its default configuration as set by its `pom.xml` file, simply run the following command in the `RansomwareResistantRemoteDocuments/Server` directory:
```bash
mvn compile exec:java
``` 

Alternatively, if you wish to set each parameter manually, you can also run the following command:

```bash
mvn compile exec:java -Dexec.mainClass="pt.tecnico.Server.ServerMain" -Dexec.args="<zooHost> <zooPort> <serverPath> <serverHost> <serverPort> <dbUser> <dbPass>"
```

- __zooHost__: The IP address of the ZooKeeper server
- __zooPort__: The port of the ZooKeeper server
- __serverPath__: The path name associated to this server, stored in the ZooKeeper server
- __serverHost__: The IP address of this server
- __serverPort__: The port of this server
- __dbUser__: The username needed to connect to the PSQL database
- __dbPass__: The password needed to connect to the PSQL database

Of note that, since the Leader Server and the file server use the exact same code files, each one of the file servers can be executed with the same commands presented above. The Leader Server is assigned as such based on whether or not it's the first server to connect to the ZooKeeper server. As such, however, it's required for the __serverPath__ field to be the exact same across all server instances.

#### 3. Client Initialization

If you wish to run the module in its default configuration as set by its `pom.xml` file, simply run the following command in the `RansomwareResistantRemoteDocuments/Client` directory:
```bash
mvn compile exec:java
``` 

Alternatively, if you wish to set each parameter manually, you can also run the following command:

```bash
mvn compile exec:java -Dexec.mainClass="pt.tecnico.Client.ClientMain" -Dexec.args="<zooHost> <zooPort> <serverPath>"
```

- __zooHost__: The IP address of the ZooKeeper server
- __zooPort__: The port of the ZooKeeper server
- __serverPath__: The path name associated to the leader/file servers, stored in the ZooKeeper server

