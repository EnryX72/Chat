# Chat Project
[Client side]
This Java-based chat server project, developed by EnryX72 and Giovanni Battistelli, is designed as an annual assignment for Professor Giachetto. 
The application provides a basic framework for a server-client chat system.
This projects aims at providing an encrypted connection among multiple clients and a server, which will interact with a database.
## Features
- Multi-client chat functionality
- Threaded server to manage multiple connections
- Basic message broadcasting to all connected clients
- Diffie-Hellman key exchange
- RSA key generation algorithm
## Requirements
- Java Development Kit (JDK) 8+
- Maven for dependency management
## Setup and Usage
Clone the repository:
```
git clone https://github.com/EnryX72/Chat_Server.git
```
Navigate into the directory:
```
cd Chat_Server
```
Compile and Run:
```
mvn clean install
java -jar target/Chat_Server.jar
```

## Project Structure
- src/: Contains the Java source code.
- pom.xml: Maven configuration file for dependencies and build.
