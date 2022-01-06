Steps to activate:
1. Install ZooKeeper (Screenshots in Discord)
2. Start ZooKeeper by going to zookeper/bin and executing `./zkServer.sh start`
3. `mvn install` in root directory
4. `mvn compile exec:java` in Server directory first, followed by the same command in Client directory