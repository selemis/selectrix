# Plugin Development Guide

This guide explains how to develop external plugins for File Selector.

## Overview

File Selector supports external plugins through a simple plugin API. Plugins are loaded dynamically from JAR files placed in the `plugins/` directory.

## Getting the Plugin API

### Option 1: Maven Local (Recommended)

From the FileSelector project root, publish the API to your local Maven repository:

```bash
./gradlew publishPluginApiPublicationToMavenLocal
```

This publishes to `~/.m2/repository` as:
- **Group ID:** `org.example.fileselector`
- **Artifact ID:** `plugin-api`
- **Version:** `1.0`

Then in your plugin project, add the dependency:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    compileOnly 'org.example.fileselector:plugin-api:1.0'
}
```

### Option 2: Direct JAR File

Alternatively, build the API JAR directly:

```bash
./gradlew pluginApiJar
```

This generates `api/file-selector-plugin-api-1.0.jar` containing:
- `FileAction` interface - Main plugin interface
- `ActionLogger` interface - Logging interface
- Source files (in the `sources/` directory for reference)

Copy this JAR to your plugin project's `lib/` directory and add:

```gradle
dependencies {
    compileOnly files('lib/file-selector-plugin-api-1.0.jar')
}
```

## Creating a Plugin

### 1. Project Setup

Create a new Java/Gradle project with the following structure:

```
your-plugin/
├── src/
│   └── main/
│       ├── java/
│       │   └── your/package/
│       │       └── YourAction.java
│       └── resources/
│           └── META-INF/services/
│               └── org.example.plugin.FileAction
├── lib/
│   └── file-selector-plugin-api-1.0.jar
└── build.gradle
```

### 2. Add API Dependency

In your `build.gradle`:

```gradle
dependencies {
    compileOnly files('lib/file-selector-plugin-api-1.0.jar')
}
```

### 3. Implement the Plugin

Create a class that implements `FileAction`:

```java
package your.package;

import org.example.plugin.ActionLogger;
import org.example.plugin.FileAction;
import java.io.File;

public class YourAction implements FileAction {

    @Override
    public String getActionName() {
        return "Your Action Name";
    }

    @Override
    public void execute(File file, ActionLogger logger) throws Exception {
        logger.info("Processing: " + file.getName());

        // Your plugin logic here

        logger.info("Completed: " + file.getName());
    }

    @Override
    public String getDescription() {
        return "Description of what your plugin does";
    }
}
```

### 4. Register the Plugin

Create the ServiceLoader configuration file at:
`src/main/resources/META-INF/services/org.example.plugin.FileAction`

Add your implementation class (fully qualified):

```
your.package.YourAction
```

### 5. Build the Plugin

Build your plugin JAR:

```bash
./gradlew jar
```

## Installing a Plugin

1. Copy your plugin JAR to the File Selector's `plugins/` directory
2. Launch File Selector
3. Your plugin will appear in the Action dropdown

## API Reference

### FileAction Interface

```java
public interface FileAction {
    /**
     * Returns the display name shown in the UI dropdown
     */
    String getActionName();

    /**
     * Executes the action on the given file
     * @param file The file to process
     * @param logger Logger for console output
     * @throws Exception if the action fails
     */
    void execute(File file, ActionLogger logger) throws Exception;

    /**
     * Returns a description of the action
     */
    String getDescription();
}
```

### ActionLogger Interface

```java
public interface ActionLogger {
    /**
     * Logs an informational message to the console
     */
    void info(String message);

    /**
     * Logs an error message to the console
     */
    void error(String message);

    /**
     * Logs an error message with exception details
     */
    void error(String message, Throwable throwable);
}
```

## Best Practices

1. **Use the Logger**: Always use the `ActionLogger` to provide feedback to users
2. **Error Handling**: Wrap risky operations in try-catch blocks and log errors
3. **File Validation**: Validate input files before processing (check existence, type, etc.)
4. **Non-Blocking**: Keep operations reasonably quick; the app runs plugins on a background thread
5. **Descriptive Names**: Use clear, descriptive names for your action

## Example Plugins

See the `unzip-plugin` project as a complete example of an external plugin implementation.

## Troubleshooting

### Plugin Not Loading

- Check that the JAR is in the `plugins/` directory
- Verify the ServiceLoader configuration file path is correct
- Check console output for error messages during plugin loading

### ClassNotFoundException

- Ensure all required classes are included in your plugin JAR
- Verify the plugin API JAR version matches the File Selector version
- Check that the ServiceLoader configuration file contains the correct fully-qualified class name

### Plugin Appears But Doesn't Work

- Check the execute() method for exceptions
- Look at console output for error messages
- Verify file validation logic is correct
