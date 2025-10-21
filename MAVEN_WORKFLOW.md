# Maven Local Workflow Guide

This guide explains the Maven Local workflow for managing the Selectrix Plugin API.

## Overview

Both the Plugin API and external plugins are managed through Maven Local (`~/.m2/repository`). This provides:
- Standard dependency management
- Version control
- Automatic resolution
- Professional development workflow
- Centralized plugin distribution

## Publishing the Plugin API

### From Selectrix Project

Publish the Plugin API to Maven Local:

```bash
cd /path/to/Selectrix
./gradlew publishPluginApiPublicationToMavenLocal
```

**Published artifact:**
- **Group ID:** `com.selectrix.selectrix`
- **Artifact ID:** `plugin-api`
- **Version:** `1.0`
- **Location:** `~/.m2/repository/org/example/selectrix/plugin-api/1.0/`

This needs to be done:
- **Once** when setting up plugin development
- **After any changes** to the plugin API interfaces (FileAction, ActionLogger)

## Publishing External Plugins

### From Plugin Project

Publish your plugin to Maven Local:

```bash
cd /path/to/your-plugin
./gradlew publishToMavenLocal
```

**Example (UnzipPlugin project):**
- **Group ID:** `com.selectrix.plugins`
- **Artifact ID:** `unzip-plugin` (lowercase per Maven conventions)
- **Version:** `1.0`
- **Location:** `~/.m2/repository/org/example/plugins/unzip-plugin/1.0/`

## Using Plugins in Selectrix

### Declaring Plugin Dependencies

In Selectrix's `build.gradle`, add plugins to the `externalPlugins` configuration:

```gradle
dependencies {
    // External plugins (fetched from Maven Local)
    externalPlugins 'com.selectrix.plugins:unzip-plugin:1.0'
    externalPlugins 'com.selectrix.plugins:another-plugin:1.0'
    // Add more plugins as needed
}
```

### Automatic Plugin Resolution

When you build Selectrix, the `copyExternalPlugins` task automatically:
1. Resolves plugin dependencies from Maven Local
2. Cleans the `plugins/` directory
3. Copies all declared plugins to `plugins/`

```bash
cd /path/to/Selectrix
./gradlew build  # Automatically copies plugins
# or
./gradlew copyExternalPlugins  # Just copy plugins
```

## Using the Plugin API in Plugins

### In build.gradle

```gradle
repositories {
    mavenLocal()    // Look in Maven Local first
    mavenCentral()  // Fallback to Maven Central
}

dependencies {
    compileOnly 'com.selectrix.selectrix:plugin-api:1.0'
}
```

### Building a Plugin

```bash
cd /path/to/your-plugin
./gradlew build
```

Gradle will automatically fetch the API from `~/.m2/repository`.

## Complete Workflow

### Initial Setup (One-Time)

1. **Publish the Plugin API** (Selectrix maintainers):
   ```bash
   cd /path/to/Selectrix
   ./gradlew publishPluginApiPublicationToMavenLocal
   ```

2. **Develop your plugin**:
   ```bash
   cd /path/to/your-plugin
   # Implement your plugin
   ```

3. **Publish your plugin to Maven Local**:
   ```bash
   cd /path/to/your-plugin
   ./gradlew publishToMavenLocal
   ```

4. **Add plugin to Selectrix** (edit `build.gradle`):
   ```gradle
   dependencies {
       externalPlugins 'com.selectrix.plugins:your-plugin:1.0'
   }
   ```

5. **Build Selectrix** (automatically copies plugins):
   ```bash
   cd /path/to/Selectrix
   ./gradlew build
   ```

### Plugin Development Cycle

**Option 1: Using Maven Local (Recommended)**
```bash
# 1. Make changes to your plugin
cd /path/to/your-plugin

# 2. Publish to Maven Local
./gradlew publishToMavenLocal

# 3. Rebuild Selectrix (copies updated plugin)
cd /path/to/Selectrix
./gradlew copyExternalPlugins

# 4. Run Selectrix to test
./gradlew run  # or launch from IDE
```

**Option 2: Quick Local Testing**
```bash
# 1. Make changes to your plugin
cd /path/to/your-plugin

# 2. Deploy directly to Selectrix (bypasses Maven)
./gradlew deployPlugin

# 3. Run Selectrix to test
cd /path/to/Selectrix
./gradlew run
```

### When API Changes

If the Selectrix team updates the plugin API:

```bash
# 1. Republish API to Maven Local
cd /path/to/Selectrix
./gradlew publishPluginApiPublicationToMavenLocal

# 2. Rebuild your plugin
cd /path/to/your-plugin
./gradlew clean build

# 3. Republish your plugin
./gradlew publishToMavenLocal

# 4. Update Selectrix
cd /path/to/Selectrix
./gradlew copyExternalPlugins
```

## Alternative: Direct JAR Distribution

If you prefer not to use Maven Local, you can still build the API as a JAR:

```bash
cd /path/to/Selectrix
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
~/.m2/repository/org/example/selectrix/plugin-api/1.0/
├── plugin-api-1.0.jar
└── plugin-api-1.0.pom
```

## Advantages of Maven Local Approach

### For Plugin API
1. **No JAR file copying** - Automatic dependency resolution
2. **Version management** - Clear versioning and compatibility
3. **Standard practice** - Industry-standard approach
4. **Cleaner repositories** - No binary files in git
5. **Multi-plugin support** - All plugins share the same API version

### For External Plugins
1. **Centralized management** - Plugins declared in build.gradle
2. **Automatic updates** - Change version, rebuild, done
3. **Clean builds** - `./gradlew clean` removes all plugins, rebuild fetches them
4. **Dependency resolution** - Gradle handles everything
5. **Version control** - Easy to track which plugin versions are used
6. **No manual copying** - Build process handles plugin deployment
7. **Team consistency** - Everyone gets the same plugins from build.gradle

## Troubleshooting

### "Could not find com.selectrix.selectrix:plugin-api:1.0"

The API hasn't been published to Maven Local. Run:
```bash
cd /path/to/Selectrix
./gradlew publishPluginApiPublicationToMavenLocal
```

### API Changes Not Reflected

Clear Gradle cache and republish:
```bash
cd /path/to/Selectrix
./gradlew clean publishPluginApiPublicationToMavenLocal

cd /path/to/your-plugin
./gradlew clean build
```

### Verify API in Maven Local

```bash
ls -l ~/.m2/repository/org/example/selectrix/plugin-api/1.0/
```

Should show:
- `plugin-api-1.0.jar`
- `plugin-api-1.0.pom`
