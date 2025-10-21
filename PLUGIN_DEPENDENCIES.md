# Plugin Dependencies Guide

This guide explains how to handle external dependencies in Selectrix plugins.

## Current Approach

Currently, the unzip plugin has **no external dependencies** - it only uses:
- Plugin API interfaces (`FileAction`, `ActionLogger`)
- Standard Java libraries (`java.io`, `java.util.zip`)

## When Plugins Need External Dependencies

If your plugin requires external libraries (e.g., Apache Commons, Jackson, etc.), use the **Fat JAR (Shadow JAR)** approach.

## Fat JAR Approach (Recommended)

### Overview

A Fat JAR bundles all dependencies into a single JAR file, making the plugin self-contained.

**Advantages:**
- ✅ Self-contained - no dependency management needed
- ✅ No version conflicts between plugins
- ✅ Easy distribution
- ✅ Works immediately without classpath configuration
- ✅ Simple for Selectrix - just load the JAR

**Disadvantages:**
- ⚠️ Larger JAR files
- ⚠️ Duplicate libraries if multiple plugins use the same dependency
- ⚠️ Slightly slower initial load time

### Implementation

To create a Fat JAR for your plugin:

#### 1. Add Shadow Plugin

In your plugin's `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'maven-publish'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}
```

#### 2. Add Dependencies

```gradle
dependencies {
    // Plugin API (compile only - provided by Selectrix)
    compileOnly 'com.selectrix.selectrix:plugin-api:1.0'

    // External dependencies (will be bundled)
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    // Add more as needed
}
```

#### 3. Configure Shadow JAR

```gradle
shadowJar {
    archiveBaseName = 'your-plugin'
    archiveVersion = project.version
    archiveClassifier = ''

    // Exclude the Plugin API (provided by Selectrix)
    dependencies {
        exclude(dependency('com.selectrix.selectrix:plugin-api:.*'))
    }

    // Relocate packages to avoid conflicts (optional but recommended)
    relocate 'org.apache.commons', 'your.plugin.shaded.commons'
    relocate 'com.fasterxml.jackson', 'your.plugin.shaded.jackson'
}

// Use shadowJar instead of regular jar
jar {
    enabled = false
}

// Make build use shadowJar
build.dependsOn shadowJar
```

#### 4. Update Publishing

```gradle
publishing {
    publications {
        plugin(MavenPublication) {
            // Publish the shadow JAR instead of regular JAR
            artifact(shadowJar)

            groupId = 'com.selectrix.plugins'
            artifactId = 'your-plugin'
            version = project.version
        }
    }

    repositories {
        mavenLocal()
    }
}
```

#### 5. Build and Publish

```bash
./gradlew shadowJar
./gradlew publishToMavenLocal
```

### Package Relocation (Advanced)

To prevent conflicts when multiple plugins use the same library, relocate (shade) packages:

```gradle
shadowJar {
    // Relocate common libraries to plugin-specific namespace
    relocate 'org.apache.commons', 'com.example.myplugin.shaded.commons'
    relocate 'com.google.gson', 'com.example.myplugin.shaded.gson'
}
```

This renames package names inside your JAR to avoid conflicts.

## Alternative Approaches (Not Recommended for Now)

### Separate Dependencies Directory

Selectrix could resolve plugin dependencies and copy them to `plugins/libs/`:

```
plugins/
├── my-plugin-1.0.jar
└── libs/
    ├── commons-lang3-3.12.0.jar
    └── jackson-databind-2.15.2.jar
```

**Issues:**
- Complex classloader setup
- Version conflict resolution needed
- More complex build configuration

### Shared Dependency Resolution

Selectrix could resolve all plugin dependencies at runtime and create a shared classpath.

**Issues:**
- Very complex implementation
- Dependency version conflicts
- Requires runtime dependency resolution framework

## Best Practices

### 1. Minimize Dependencies

Keep plugin dependencies minimal. Ask yourself:
- Do I really need this library?
- Can I use Java standard library instead?
- Is this dependency too large for a plugin?

### 2. Use CompileOnly for Plugin API

Always mark the Plugin API as `compileOnly`:

```gradle
dependencies {
    compileOnly 'com.selectrix.selectrix:plugin-api:1.0'  // NOT implementation
}
```

The Plugin API is provided by Selectrix at runtime.

### 3. Document Dependencies

In your plugin's README, list all external dependencies:

```markdown
## Dependencies

This plugin uses:
- Apache Commons Lang 3.12.0 (bundled)
- Jackson Databind 2.15.2 (bundled)
```

### 4. Watch JAR Size

Fat JARs can get large. Monitor your plugin JAR size:

```bash
ls -lh build/libs/your-plugin-1.0.jar
```

If it's > 10MB, consider:
- Using lighter alternatives
- Excluding unnecessary transitive dependencies
- Minimizing dependency scope

### 5. Test in Isolation

Always test your plugin in a clean Selectrix environment to ensure all dependencies are bundled correctly.

## Example: Plugin with Dependencies

Here's a complete example for a plugin that uses Apache Commons:

**build.gradle:**
```gradle
plugins {
    id 'java'
    id 'maven-publish'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

group = 'com.selectrix.plugins'
version = '1.0'

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly 'com.selectrix.selectrix:plugin-api:1.0'
    implementation 'org.apache.commons:commons-lang3:3.12.0'
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

shadowJar {
    archiveBaseName = 'my-plugin'
    archiveVersion = version
    archiveClassifier = ''

    dependencies {
        exclude(dependency('com.selectrix.selectrix:plugin-api:.*'))
    }

    relocate 'org.apache.commons', 'com.example.myplugin.shaded.commons'
}

jar.enabled = false
build.dependsOn shadowJar

publishing {
    publications {
        plugin(MavenPublication) {
            artifact(shadowJar)
            groupId = group
            artifactId = 'my-plugin'
            version = version
        }
    }

    repositories {
        mavenLocal()
    }
}
```

## Summary

- **No dependencies needed?** Use current simple approach (like unzip plugin)
- **Need dependencies?** Use Fat JAR with Shadow plugin
- **Future consideration:** Explore shared dependency resolution if needed

For most plugins, Fat JAR is the sweet spot between simplicity and functionality.
