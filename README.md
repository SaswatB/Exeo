This was a learning project for me during college, it is not in a complete state at this time.


# Exeo Server
Exeo is a platform for managing information flowing between devices, from sending files between computers to managing a smart home.

This is the server behind the website for exeo.io, which uses Spring, WebPack, MySQL

## Getting Started
### Prerequisites
 - [Git](https://git-scm.com/)
 - [MySQL](https://www.mysql.com/)
 - [Redis](http://redis.io/)
 - [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html)
 - [IntelliJ](https://www.jetbrains.com/idea/) or another IDE
 - [NPM](https://nodejs.org/en/)
 - [WebPack](https://webpack.github.io/) (`npm install -g webpack`)
 - [Bower](https://bower.io/) (`npm install -g bower`)

### Installation
#### Databases
Import the schema for the MySQL server.

To properly have the Exeo server connect to your MySQL instance configure the connection properties within application.properties.
Please do not commit your changes to this file.

Both the Redis server and the MySQL server need to be running for the Exeo server to start.

#### Javascript
Go to the following directory: `src\main\webapp` 

and type the following commands:
```
npm install
bower install
```
This will set up the necessary Javascript dependencies.

### Building and Running
Go to the following directory: `src\main\webapp` 
and run the following command
```
webpack -d watch
```
Then in a new terminal run the following command in the project's base directory:
```
./gradle assemble
java -jar build/libs/exeo-0.0.1-SNAPSHOT.jar
```
If you are using IntelliJ simply use the run command.

The Exeo server should now be running on port 80.
