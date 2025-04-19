# EventHub Utility

EventHub Utility is a Java-based library designed to streamline interactions with Azure Event Hubs. It is intended for use in Java applications, including Azure Function Apps, to facilitate event-driven architectures.

## Features

- Simplified integration with Azure Event Hubs for Java applications.
- Send and receive messages with minimal configuration.
- Support for managing Event Hub connections and configurations.
- Robust logging and error handling for improved debugging and monitoring.

## Use Case

This utility is designed to be used in Java-based applications, such as Azure Function Apps, to handle event-driven workflows. It simplifies the process of sending and receiving messages to and from Azure Event Hubs, enabling developers to focus on business logic rather than infrastructure details.

## Importing the Utility

Once the utility is packaged as a JAR file, follow these steps to import it into your Java project:

1. Add the JAR file to your project's `lib` directory or include it in your build tool's dependencies (e.g., Maven or Gradle).

2. If using Maven, add the following dependency to your `pom.xml`:
    ```xml
    <dependency>
         <groupId>com.example</groupId>
         <artifactId>eventhub-utility</artifactId>
         <version>1.0.0</version>
    </dependency>
    ```

3. If using Gradle, add the following to your `build.gradle`:
    ```gradle
    implementation 'com.example:eventhub-utility:1.0.0'
    ```

4. Configure your application to use the utility by providing the necessary environment variables:
    - `EVENTHUB_CONNECTION_STRING`: Your Azure Event Hub connection string.
    - `EVENTHUB_NAME`: The name of your Event Hub instance.

5. Import the utility classes in your Java code and start using its features:
    ```java
    import com.example.eventhubutility.EventHubClient;

    public class Main {
         public static void main(String[] args) {
              EventHubClient client = new EventHubClient();
              client.sendMessage("Hello, Event Hub!");
         }
    }
    ```

For detailed usage examples, refer to the documentation or sample code provided with the utility.
