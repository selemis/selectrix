# Maven Local Workflow Guide

This guide explains the Maven Local workflow for managing the File Selector Plugin API.

## Overview

The Plugin API is published to Maven Local (`~/.m2/repository`) instead of being distributed as a JAR file. This provides:
- Standard dependency management
- Version control
- Automatic resolution
- Professional development workflow

## Publishing the Plugin API

### From FileSelector Project

Publish the Plugin API to Maven Local:

```bash
cd /path/to/FileSelector
./gradlew publishPluginApiPublicationToMavenLocal
```

**Published artifact:**
- **Group ID:** `org.example.fileselector`
- **Artifact ID:** `plugin-api`
- **Version:** `1.0`
- **Location:** `~/.m2/repository/org/example/fileselector/plugin-api/1.0/`

This needs to be done:
- **Once** when setting up plugin development
- **After any changes** to the plugin API interfaces (FileAction, ActionLogger)

## Using the Plugin API in Plugins

### In build.gradle

```gradle
repositories {
    mavenLocal()    // Look in Maven Local first
    mavenCentral()  // Fallback to Maven Central
}

dependencies {
    compileOnly 'org.example.fileselector:plugin-api:1.0'
}
```

### Building a Plugin

```bash
cd /path/to/your-plugin
./gradlew build
```

Gradle will automatically fetch the API from `~/.m2/repository`.

## Workflow for Plugin Developers

### Initial Setup

1. Clone or create your plugin project
2. Ensure the Plugin API is published to Maven Local:
   ```bash
   cd /path/to/FileSelector
   ./gradlew publishPluginApiPublicationToMavenLocal
   ```
3. Build your plugin:
   ```bash
   cd /path/to/your-plugin
   ./gradlew build
   ```

### Development Cycle

```bash
# Make changes to your plugin
# Build and deploy
./gradlew deployPlugin

# Test in File Selector application
```

### When API Changes

If the FileSelector team updates the plugin API:

```bash
# Update API in Maven Local
cd /path/to/FileSelector
./gradlew publishPluginApiPublicationToMavenLocal

# Rebuild your plugin
cd /path/to/your-plugin
./gradlew clean build
```

## Alternative: Direct JAR Distribution

If you prefer not to use Maven Local, you can still build the API as a JAR:

```bash
cd /path/to/FileSelector
./gradlew pluginApiJar
```

Output: `api/file-selector-plugin-api-1.0.jar`

Then in your plugin:

```gradle
dependencies {
    compileOnly files('lib/file-selector-plugin-api-1.0.jar')
}
```

## Maven Local Location

The Plugin API is published to:
```
~/.m2/repository/org/example/fileselector/plugin-api/1.0/
├── plugin-api-1.0.jar
└── plugin-api-1.0.pom
```

## Advantages of Maven Local Approach

1. **No JAR file copying** - Automatic dependency resolution
2. **Version management** - Clear versioning and compatibility
3. **Standard practice** - Industry-standard approach
4. **Cleaner repositories** - No binary files in git
5. **Multi-plugin support** - All plugins share the same API version

## Troubleshooting

### "Could not find org.example.fileselector:plugin-api:1.0"

The API hasn't been published to Maven Local. Run:
```bash
cd /path/to/FileSelector
./gradlew publishPluginApiPublicationToMavenLocal
```

### API Changes Not Reflected

Clear Gradle cache and republish:
```bash
cd /path/to/FileSelector
./gradlew clean publishPluginApiPublicationToMavenLocal

cd /path/to/your-plugin
./gradlew clean build
```

### Verify API in Maven Local

```bash
ls -l ~/.m2/repository/org/example/fileselector/plugin-api/1.0/
```

Should show:
- `plugin-api-1.0.jar`
- `plugin-api-1.0.pom`
