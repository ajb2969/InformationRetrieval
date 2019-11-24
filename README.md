# InformationRetrieval
## Pre-requisites
The pre-requisites required to run this project are:

* Java 8
* Maven 3.6.2

## Build
Run the command 
```java
$mvn compile
```

Then run,
```java
$mvn package
```

The latter of the two commands will produce a .jar file in the target directory.

## Run
Run the command
```java
$java -jar target/*.jar
```

This java command will start the program and open tomcat to listen on port 8080. 

To view the webpage, visit ` http://localhost:8080 ` in your browser.