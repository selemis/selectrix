# Selectrix

A Java Swing application that allows users to browse folders, select multiple files/folders via checkboxes, and perform batch operations on them using a pluggable action system.

## Features

- **Folder Browsing**: Open any folder and view its contents in a table
- **File Information Display**: Shows file type, name, size, and last modified date
- **Checkbox Selection**: Select multiple files/folders for batch processing
- **Visual Feedback**: Selected items are highlighted in light blue
- **Bulk Operations**: Select All / Deselect All buttons for quick selection
- **Integrated Console**: Real-time output display in a terminal-style console at the bottom of the window
- **Pluggable Actions**: Extensible architecture allows adding custom actions via plugins

## Built-in Actions

The application comes with the following built-in actions:

1. **Print Filenames** - Prints the names of selected files to the console
2. **Copy Files** - (Placeholder) Copy selected files to a destination
3. **Move Files** - (Placeholder) Move selected files to a destination
4. **Delete Files** - (Placeholder) Delete selected files

## How to Use

1. **Launch the application**
   ```bash
   ./gradlew run
   ```

2. **Open a folder**
   - Click `File > Open Folder`
   - Select the folder you want to browse

3. **Select files**
   - Click checkboxes next to files you want to process
   - Use "Select All" or "Deselect All" buttons for bulk selection

4. **Choose an action**
   - Select an action from the "Action" dropdown
   - Click "Process Files" to execute the action on all selected files

5. **View output**
   - All processing output appears in the console at the bottom of the window
   - The console auto-scrolls to show the latest messages
   - You can resize the console by dragging the divider between the table and console

## Plugin System

### Architecture Overview

The application uses a plugin-based architecture that allows you to extend functionality by adding custom actions without modifying the core application code.

### Plugin Interfaces

All plugins must implement the `FileAction` interface and use the `ActionLogger` for output:

```java
package com.selectrix.plugin;

import java.io.File;

public interface FileAction {
    /**
     * Returns the display name of this action that will appear in the UI.
     */
    String getActionName();

    /**
     * Executes the action on the given file.
     * @param file The file to process
     * @param logger Logger for outputting messages to the GUI console
     */
    void execute(File file, ActionLogger logger) throws Exception;

    /**
     * Returns a description of what this action does.
     */
    String getDescription();
}
```

```java
package com.selectrix.plugin;

public interface ActionLogger {
    /**
     * Logs an informational message to the GUI console.
     */
    void info(String message);

    /**
     * Logs an error message to the GUI console.
     */
    void error(String message);

    /**
     * Logs an error message with exception details to the GUI console.
     */
    void error(String message, Throwable throwable);
}
```

### Creating a Plugin

#### Step 1: Create a New Java Project

Create a separate Java project for your plugin.

#### Step 2: Add Selectrix as a Dependency

You need access to the `FileAction` and `ActionLogger` interfaces. Either:
- Add the Selectrix project as a dependency
- Copy both `FileAction.java` and `ActionLogger.java` interfaces to your plugin project

#### Step 3: Implement the FileAction Interface

Example - Creating an "Unzip Files" plugin:

```java
package com.mycompany.plugins;

import com.selectrix.plugin.ActionLogger;
import com.selectrix.plugin.FileAction;
import java.io.File;
import java.util.zip.ZipFile;
// ... other imports for unzipping

public class UnzipAction implements FileAction {

    @Override
    public String getActionName() {
        return "Unzip Files";
    }

    @Override
    public void execute(File file, ActionLogger logger) throws Exception {
        if (file.getName().endsWith(".zip")) {
            logger.info("Unzipping: " + file.getName());

            // Your unzip logic here
            try {
                // ... actual unzip code
                logger.info("  Successfully extracted: " + file.getName());
            } catch (Exception e) {
                logger.error("  Failed to extract: " + file.getName(), e);
                throw e;
            }
        } else {
            logger.info("  Skipping non-ZIP file: " + file.getName());
        }
    }

    @Override
    public String getDescription() {
        return "Extracts ZIP files to the same directory";
    }
}
```

**Important**: Always use the `logger` parameter instead of `System.out.println()` or `System.err.println()`. This ensures your output appears in the GUI console where users can see it.

#### Step 4: Create ServiceLoader Configuration

Create a file at: `src/main/resources/META-INF/services/com.selectrix.plugin.FileAction`

Content (one class per line):
```
com.mycompany.plugins.UnzipAction
```

If you have multiple plugins in the same JAR, list them all:
```
com.mycompany.plugins.UnzipAction
com.mycompany.plugins.EncryptAction
com.mycompany.plugins.CompressAction
```

#### Step 5: Build the JAR

Build your plugin as a JAR file, including all dependencies:

```bash
./gradlew jar
# or with dependencies
./gradlew shadowJar  # if using shadow plugin
```

#### Step 6: Deploy the Plugin

1. Copy the JAR file to the `plugins/` directory in the Selectrix project root
2. Restart the Selectrix application
3. Your plugin will automatically appear in the "Action" dropdown!

### Plugin Dependencies

Plugins can have their own dependencies. When packaging your plugin:

1. **Option A: Fat JAR (Recommended)**
   - Use a plugin like Gradle Shadow or Maven Shade
   - Bundle all dependencies into a single JAR
   - Drop the single JAR in `plugins/`

2. **Option B: Multiple JARs**
   - Put your plugin JAR and all dependency JARs in `plugins/`
   - The URLClassLoader will load all JARs from the directory

Example `build.gradle` for a plugin with dependencies:

```gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

dependencies {
    compileOnly files('path/to/Selectrix.jar') // For FileAction interface
    implementation 'org.apache.commons:commons-compress:1.24.0' // Your dependency
}

shadowJar {
    archiveBaseName.set('unzip-plugin')
    archiveClassifier.set('')
    archiveVersion.set('1.0')
}
```

## Technical Documentation

### How the Plugin System Works

#### Custom ClassLoader

**Why is it needed?**

Java's default ClassLoader only loads classes from the application's classpath (compiled code and dependencies defined in `build.gradle`). JAR files in external directories like `plugins/` are not on the classpath.

**URLClassLoader Solution:**

```java
URL[] urls = ... // URLs pointing to plugin JAR files
URLClassLoader pluginClassLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader());
```

The `URLClassLoader`:
- Loads classes from the specified JAR files at runtime
- Has the application's ClassLoader as its parent, so plugins can access the `FileAction` interface
- Isolates plugin dependencies from the main application
- Allows plugins to bundle their own dependencies without conflicts

**Example Use Case:**

If you create a plugin that uses Apache Commons Compress library:
- Bundle Commons Compress in your plugin JAR
- URLClassLoader loads both your plugin class and the library
- No need to add Commons Compress to the main application's dependencies
- Multiple plugins can use different versions of the same library without conflicts

#### ServiceLoader

**What is ServiceLoader?**

`ServiceLoader` is Java's built-in Service Provider Interface (SPI) mechanism for discovering and loading implementations of an interface at runtime.

**How it works:**

1. **Provider Declaration**: Plugin JARs contain a special file:
   ```
   META-INF/services/com.selectrix.plugin.FileAction
   ```

   This file lists all implementations:
   ```
   com.mycompany.plugins.UnzipAction
   com.mycompany.plugins.EncryptAction
   ```

2. **Service Discovery**: The application uses ServiceLoader:
   ```java
   ServiceLoader<FileAction> serviceLoader = ServiceLoader.load(FileAction.class, pluginClassLoader);

   for (FileAction plugin : serviceLoader) {
       // ServiceLoader automatically instantiates each implementation
       availableActions.add(plugin);
   }
   ```

3. **Automatic Discovery**: No hardcoded class names needed! Just drop a JAR in `plugins/` and it's automatically discovered.

**Benefits:**

- **No Configuration**: No need to update application code or config files when adding plugins
- **Decoupling**: Application doesn't need to know plugin class names
- **Standard Java**: Uses the official Java SPI mechanism
- **Type Safety**: Ensures all plugins implement the required interface

**Alternative Without ServiceLoader:**

Without ServiceLoader, you would need to:
```java
// Manually specify class names
String className = "com.mycompany.plugins.UnzipAction";
Class<?> clazz = pluginClassLoader.loadClass(className);
FileAction plugin = (FileAction) clazz.getDeclaredConstructor().newInstance();
```

And maintain a registry/config file of all plugin class names.

### Plugin Loading Flow

1. **Application Startup** (`Main.java` constructor)
   - Calls `PluginLoader.loadPlugins()`

2. **Load Built-in Plugins** (`PluginLoader.loadPlugins()`)
   - Instantiates built-in actions (PrintFilenameAction, CopyFileAction, etc.)

3. **Scan Plugins Directory** (`PluginLoader.loadExternalPlugins()`)
   - Finds all `.jar` files in `plugins/` directory
   - Creates `URL[]` array with paths to all JARs

4. **Create ClassLoader**
   - Creates `URLClassLoader` with plugin JAR URLs
   - Sets application ClassLoader as parent

5. **Discover Implementations**
   - Uses `ServiceLoader.load(FileAction.class, pluginClassLoader)`
   - ServiceLoader reads `META-INF/services/com.selectrix.plugin.FileAction` from each JAR
   - Instantiates all listed implementations

6. **Populate UI**
   - All plugins (built-in + external) are added to the Action dropdown
   - User can select and execute any loaded plugin

7. **Execute Plugin**
   - When user clicks "Process Files", the console is cleared
   - A `ConsoleLogger` instance is created
   - The plugin's `execute()` method is called for each selected file, passing the logger
   - All logger output appears in real-time in the GUI console

## Project Structure

```
Selectrix/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           ├── Main.java                    # Main application window with GUI console
│   │           └── plugin/
│   │               ├── FileAction.java          # Plugin interface
│   │               ├── ActionLogger.java        # Logger interface for plugins
│   │               ├── PluginLoader.java        # Plugin loading logic
│   │               └── impl/                    # Built-in plugins
│   │                   ├── PrintFilenameAction.java
│   │                   ├── CopyFileAction.java
│   │                   ├── MoveFileAction.java
│   │                   └── DeleteFileAction.java
│   └── test/
│       └── groovy/                              # Spock tests
├── plugins/                                     # External plugin JARs go here
├── build.gradle                                 # Gradle build configuration
└── README.md                                    # This file
```

## For End Users (No Development Environment Required)

### Requirements

- Java Runtime Environment (JRE) 11 or higher
  - **Windows**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
  - **macOS**: Download from [Adoptium](https://adoptium.net/) or use `brew install openjdk@11`
  - **Linux**: Use your package manager (e.g., `sudo apt install openjdk-11-jre`)

### Getting the Application

**Option 1: Download Pre-built Distribution**
- Ask the developer for the `Selectrix-1.0-SNAPSHOT.zip` file

**Option 2: Build from Source** (see Developer section below)

### Installation

1. Extract the ZIP file to your desired location

2. The extracted folder contains:
   ```
   Selectrix/
   ├── bin/
   │   ├── Selectrix       (Linux/Mac launcher)
   │   └── Selectrix.bat   (Windows launcher)
   ├── lib/
   │   └── (application JARs)
   ├── plugins/
   │   └── (plugin JARs)
   └── README.md
   ```

### Running the Application

**Windows:**
- Double-click `bin/Selectrix.bat`
- Or open Command Prompt and run:
  ```
  cd path\to\Selectrix
  bin\Selectrix.bat
  ```

**Linux/macOS:**
- Open Terminal and run:
  ```bash
  cd path/to/Selectrix
  chmod +x bin/Selectrix  # First time only
  ./bin/Selectrix
  ```

### Troubleshooting

**Application won't start:**
- Verify Java is installed: `java -version`
- Ensure Java 11 or higher is installed
- On Linux/Mac, ensure the launcher script is executable: `chmod +x bin/Selectrix`

**Plugins not loading:**
- Check that plugin JARs are in the `plugins/` directory
- Check console output for error messages

## For Developers

### Building the Project

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test

# Run all quality checks
./gradlew qualityCheck

# Start SonarQube server (Docker required)
./start-sonarqube.sh

# Create distribution ZIP (for sharing with others)
./gradlew distZip

# Create distribution TAR
./gradlew distTar
```

The distribution file will be created at:
- **ZIP**: `build/distributions/Selectrix-1.0-SNAPSHOT.zip`
- **TAR**: `build/distributions/Selectrix-1.0-SNAPSHOT.tar`

### Sharing with Non-Developers

1. Build the distribution:
   ```bash
   ./gradlew distZip
   ```

2. Share the ZIP file from `build/distributions/`

3. Recipients only need Java installed (no Gradle or IDE required)

### Development Requirements

- Java 17 or later
- Gradle 8.14 or later

## Testing

The project uses Spock Framework for testing. Tests are located in `src/test/groovy/`.

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with code coverage report
./gradlew testWithCoverage
```

### Code Coverage

The project uses JaCoCo for code coverage analysis. After running tests, coverage reports are automatically generated.

**Viewing Coverage Reports:**

- **HTML Report**: Open `build/reports/jacoco/test/html/index.html` in your browser
  - Provides a visual, interactive report with line-by-line coverage
  - Shows coverage by package, class, and method
  - Color-coded: green (covered), red (not covered), yellow (partially covered)

- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml`
  - Machine-readable format for CI/CD integration
  - Can be used with SonarQube, Jenkins, or other analysis tools

**Understanding Coverage Metrics:**

- **Instructions**: Individual Java bytecode instructions
- **Branches**: Decision points (if/else, switch, loops)
- **Lines**: Source code lines
- **Methods**: Individual methods/functions
- **Classes**: Number of classes

**Tips for Improving Coverage:**

- Focus on testing business logic, utility classes, and models
- UI components (especially Swing classes) are typically harder to test
- Aim for meaningful tests rather than just high coverage numbers

### Static Code Analysis (PMD)

The project uses PMD for static code analysis to detect potential bugs, code smells, and best practice violations.

**Running PMD:**

```bash
# Run PMD analysis
./gradlew pmdCheck

# Run all quality checks (tests, coverage, and PMD)
./gradlew qualityCheck
```

**Viewing PMD Reports:**

- **HTML Report**: Open `build/reports/pmd/main.html` in your browser
  - Shows all violations grouped by file
  - Includes rule descriptions and line numbers
  - Color-coded by priority (high, medium, low)

- **XML Report**: `build/reports/pmd/main.xml`
  - Machine-readable format for CI/CD integration
  - Can be integrated with Jenkins, SonarQube, etc.

**PMD Rule Categories:**

The project uses a custom ruleset (`config/pmd/ruleset.xml`) that checks for:

- **Best Practices**: Common programming mistakes and anti-patterns
- **Code Style**: Coding standards and conventions
- **Design**: Object-oriented design principles
- **Error Prone**: Code that is likely to cause bugs
- **Performance**: Performance issues and inefficiencies
- **Security**: Security vulnerabilities
- **Multithreading**: Concurrency and thread safety issues

**Understanding PMD Violations:**

PMD reports violations with:
- **File and line number**: Where the issue occurs
- **Rule name**: Which rule was violated
- **Description**: Explanation of the problem
- **Priority**: 1 (High) to 5 (Low)

**Common Violations and How to Fix:**

- **SystemPrintln**: Replace `System.out.println()` with proper logging
- **AvoidCatchingGenericException**: Catch specific exceptions instead of `Exception`
- **ControlStatementBraces**: Add braces to if/else/for/while statements
- **UseUtilityClass**: Make utility classes final with private constructor

**Customizing Rules:**

Edit `config/pmd/ruleset.xml` to:
- Enable/disable specific rules
- Adjust rule priorities
- Configure rule parameters
- Exclude rules that don't fit your project

### Bug Detection (SpotBugs)

The project uses SpotBugs (successor to FindBugs) to detect potential bugs in Java bytecode.

**Running SpotBugs:**

```bash
# Run SpotBugs analysis
./gradlew spotbugsCheck

# Run all quality checks (tests, coverage, PMD, and SpotBugs)
./gradlew qualityCheck
```

**Viewing SpotBugs Reports:**

- **HTML Report**: Open `build/reports/spotbugs/main.html` in your browser
  - Interactive report with bug details and explanations
  - Grouped by bug category and severity
  - Includes source code snippets showing the issue
  - Links to detailed bug pattern descriptions

- **XML Report**: `build/reports/spotbugs/main.xml`
  - Machine-readable format for CI/CD integration
  - Compatible with SonarQube, Jenkins, and other tools

**SpotBugs Configuration:**

- **Effort Level**: `max` - Performs thorough analysis (may take longer)
- **Report Level**: `medium` - Reports medium and high priority bugs
- **Ignore Failures**: `true` - Won't fail the build on bugs (good for starting out)

**Bug Categories:**

SpotBugs detects issues in the following categories:

- **Correctness**: Probable bugs - violations of expected behavior
  - Null pointer dereferences
  - Infinite loops
  - Incorrect method implementations

- **Bad Practice**: Code that violates recommended practices
  - Missing null checks
  - Incorrect equals/hashCode implementations
  - Ignoring return values

- **Performance**: Code that may perform poorly
  - Inefficient string concatenation
  - Unnecessary object creation
  - Boxing/unboxing issues

- **Multithreaded Correctness**: Thread safety issues
  - Race conditions
  - Synchronization problems
  - Volatile field usage

- **Malicious Code Vulnerability**: Security vulnerabilities
  - Field exposure
  - Mutable objects returned
  - Public static fields

- **Dodgy Code**: Confusing or error-prone code
  - Dead local stores
  - Useless control flow
  - Redundant comparisons

**Understanding Bug Rankings:**

- **Scariest (Rank 1-4)**: Critical issues that should be fixed immediately
- **Scary (Rank 5-9)**: Important issues that likely indicate bugs
- **Troubling (Rank 10-14)**: Potential issues worth investigating
- **Of Concern (Rank 15-20)**: Minor issues or code smells

**Common SpotBugs Issues:**

- **NP_NULL_ON_SOME_PATH**: Possible null pointer dereference
- **RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE**: Null check after dereference
- **URF_UNREAD_FIELD**: Field is never read after being written
- **DM_DEFAULT_ENCODING**: Reliance on default character encoding
- **EI_EXPOSE_REP**: May expose internal representation

### Comprehensive Quality Dashboard (SonarQube)

SonarQube provides a centralized dashboard that aggregates all quality metrics from JaCoCo, PMD, SpotBugs, and more.

**SonarQube Options:**

1. **Local SonarQube Server with Docker** (Recommended)
   - Self-hosted, full control
   - Free for all projects (no limitations)
   - Easy setup with Docker
   - Professional-grade dashboard

2. **SonarCloud** (Alternative for Open Source)
   - Free for public repositories only
   - Cloud-hosted, no setup required
   - Visit [sonarcloud.io](https://sonarcloud.io)

**Setting Up SonarQube with Docker:**

**Prerequisites:**
- Docker installed on your machine
  - **Windows**: [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)
  - **macOS**: [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
  - **Linux**: `sudo apt-get install docker.io` (Ubuntu/Debian) or see [Docker docs](https://docs.docker.com/engine/install/)

**Quick Start (Recommended):**

Use the provided script to start SonarQube automatically:

```bash
# Make the script executable (first time only)
chmod +x start-sonarqube.sh

# Start SonarQube
./start-sonarqube.sh
```

The script will:
- Start SonarQube container if not exists
- Wait for SonarQube to be ready
- Display the URL and next steps

**Manual Setup:**

**Step 1: Start SonarQube Server**

```bash
# Pull and run SonarQube container
docker run -d --name sonarqube \
  -p 9000:9000 \
  -v sonarqube_data:/opt/sonarqube/data \
  -v sonarqube_extensions:/opt/sonarqube/extensions \
  -v sonarqube_logs:/opt/sonarqube/logs \
  sonarqube:latest

# Wait about 2 minutes for SonarQube to start
# Check if it's ready
docker logs -f sonarqube
# Wait for message: "SonarQube is operational"
# Press Ctrl+C to stop following logs
```

**Step 2: Access SonarQube Web Interface**

1. Open your browser and go to: [http://localhost:9000](http://localhost:9000)
2. Login with default credentials:
   - **Username**: `admin`
   - **Password**: `admin`
3. You'll be prompted to change the password - choose a new password

**Step 3: Create a Project**

1. Click "Create Project" → "Manually"
2. Enter project details:
   - **Project display name**: `Selectrix`
   - **Project key**: `selectrix`
   - Click "Set Up"
3. Choose "Locally"
4. Generate a token:
   - **Token name**: `selectrix-token`
   - Click "Generate"
   - **IMPORTANT**: Copy the token immediately (you won't see it again!)

**Step 4: Run Analysis**

```bash
# First, generate all quality reports
./gradlew qualityCheck

# Upload to SonarQube (replace YOUR_TOKEN with the token from Step 3)
./gradlew sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN
```

**Step 5: View Results**

1. Go to [http://localhost:9000](http://localhost:9000)
2. Click on "Projects" → "Selectrix"
3. Explore the dashboard showing:
   - 🐛 Bugs and vulnerabilities
   - 💩 Code smells
   - 📊 Code coverage
   - 🔄 Code duplications
   - ⏱️ Technical debt

**Managing SonarQube Container:**

```bash
# Stop SonarQube
docker stop sonarqube

# Start SonarQube (after stopping)
docker start sonarqube

# Remove SonarQube (if you want to start fresh)
docker rm -f sonarqube
# Note: This will delete all data unless you used volumes

# View logs
docker logs sonarqube
```

**Troubleshooting Docker Setup:**

**Issue: "Cannot connect to Docker daemon"**
- **Windows/Mac**: Make sure Docker Desktop is running
- **Linux**: Start Docker service: `sudo systemctl start docker`

**Issue: Port 9000 already in use**
```bash
# Use a different port (e.g., 9001)
docker run -d --name sonarqube \
  -p 9001:9000 \
  sonarqube:latest
# Then access at http://localhost:9001
```

**Issue: SonarQube won't start (container keeps restarting)**
```bash
# Check system requirements
# SonarQube requires:
# - At least 2GB RAM allocated to Docker
# - vm.max_map_count >= 262144 (Linux only)

# For Linux, set vm.max_map_count:
sudo sysctl -w vm.max_map_count=262144

# For Docker Desktop (Windows/Mac):
# Increase memory in Docker Desktop Settings → Resources → Memory to at least 4GB
```

**Issue: "Elasticsearch: max virtual memory areas too low"**
```bash
# Linux only - increase max_map_count permanently
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

**What SonarQube Analyzes:**

- **Code Coverage**: From JaCoCo reports
- **Code Smells**: From PMD and SonarQube's own analyzers
- **Bugs**: From SpotBugs and SonarQube
- **Security Vulnerabilities**: Security hotspots and vulnerabilities
- **Duplications**: Duplicate code blocks
- **Complexity**: Cyclomatic complexity metrics
- **Maintainability**: Technical debt and maintainability ratings

**SonarQube Quality Gate:**

SonarQube provides a "Quality Gate" that determines if your code meets minimum standards:
- Coverage on new code > 80%
- Duplicated lines on new code < 3%
- Maintainability rating = A
- Reliability rating = A
- Security rating = A

**Viewing Results:**

After running the analysis, view the dashboard at:
- **SonarCloud**: `https://sonarcloud.io/dashboard?id=selectrix`
- **Local Server**: `http://localhost:9000/dashboard?id=selectrix`

**Continuous Integration:**

Add to your CI/CD pipeline (GitHub Actions, Jenkins, GitLab CI):

```yaml
# Example GitHub Actions workflow
- name: Build and analyze
  run: ./gradlew build sonar
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Project Configuration:**

The SonarQube configuration is in `build.gradle`:
- Project key: `selectrix`
- Automatically imports JaCoCo, PMD, and SpotBugs reports
- Excludes `Main.java` from analysis (GUI main class)
- Configured for Java 17

## Quality Metrics Summary

The project now has comprehensive quality tooling:

| Tool | Purpose | Command | Report Location |
|------|---------|---------|-----------------|
| **JaCoCo** | Code Coverage | `./gradlew testWithCoverage` | `build/reports/jacoco/test/html/index.html` |
| **PMD** | Static Analysis | `./gradlew pmdCheck` | `build/reports/pmd/main.html` |
| **SpotBugs** | Bug Detection | `./gradlew spotbugsCheck` | `build/reports/spotbugs/main.html` |
| **SonarQube** | Quality Dashboard | `./gradlew sonar` | SonarQube web UI |
| **All** | All Checks | `./gradlew qualityCheck` | Multiple reports |

**Recommended Workflow:**

1. **During Development**: Run `./gradlew testWithCoverage` frequently
2. **Before Committing**: Run `./gradlew qualityCheck` to check all metrics
3. **In CI/CD**: Run `./gradlew qualityCheck sonar` to upload to SonarQube
4. **Weekly**: Review SonarQube dashboard and address high-priority issues

## GUI Console

The application includes an integrated console at the bottom of the window that displays real-time output from plugins.

### Console Features

- **Terminal-style Display**: Black background with green text for easy readability
- **Auto-scroll**: Automatically scrolls to show the latest messages
- **Resizable**: Drag the divider between the table and console to adjust sizes
- **Message Levels**: Displays `[INFO]` and `[ERROR]` prefixes for different message types
- **Exception Details**: Shows exception class and message for errors
- **Clear on Run**: Console is automatically cleared before each processing run

### Console Implementation

The console is implemented using a `JTextArea` in a `JSplitPane`. The `ConsoleLogger` class implements the `ActionLogger` interface and writes messages to this text area using `SwingUtilities.invokeLater()` to ensure thread-safe GUI updates.

All plugins receive an `ActionLogger` instance and should use it instead of `System.out` or `System.err` to ensure output is visible to users in the GUI.

## Future Enhancements

- Implement Copy Files functionality
- Implement Move Files functionality
- Implement Delete Files functionality (with confirmation)
- Add plugin configuration UI
- Add progress bar for long-running operations
- Add ability to export/import selection lists
- Add file filtering capabilities
- Add recursive folder processing
- Add console export/save functionality
- Add different console color schemes

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
