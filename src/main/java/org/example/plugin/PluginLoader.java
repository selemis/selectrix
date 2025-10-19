package org.example.plugin;

import org.example.plugin.impl.CopyFileAction;
import org.example.plugin.impl.DeleteFileAction;
import org.example.plugin.impl.MoveFileAction;
import org.example.plugin.impl.PrintFilenameAction;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads FileAction plugins from the built-in implementations and from JAR files
 * in the plugins directory.
 */
public class PluginLoader {
    private static final String PLUGINS_DIR = "plugins";

    /**
     * Loads all available plugins.
     * First loads built-in plugins, then scans the plugins directory for JAR files.
     *
     * @return List of all loaded FileAction plugins
     */
    public static List<FileAction> loadPlugins() {
        List<FileAction> plugins = new ArrayList<>();

        // Load built-in plugins
        plugins.add(new PrintFilenameAction());
        plugins.add(new CopyFileAction());
        plugins.add(new MoveFileAction());
        plugins.add(new DeleteFileAction());

        // Load external plugins from plugins directory
        plugins.addAll(loadExternalPlugins());

        return plugins;
    }

    /**
     * Loads plugins from JAR files in the plugins directory.
     * Uses ServiceLoader to discover implementations of FileAction interface.
     *
     * @return List of externally loaded plugins
     */
    private static List<FileAction> loadExternalPlugins() {
        List<FileAction> externalPlugins = new ArrayList<>();
        File pluginsDir = new File(PLUGINS_DIR);

        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            System.out.println("Plugins directory not found: " + PLUGINS_DIR);
            return externalPlugins;
        }

        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No plugin JAR files found in: " + PLUGINS_DIR);
            return externalPlugins;
        }

        try {
            // Create URLs for all JAR files
            URL[] urls = new URL[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++) {
                urls[i] = jarFiles[i].toURI().toURL();
                System.out.println("Loading plugin: " + jarFiles[i].getName());
            }

            // Create a class loader with all plugin JARs
            URLClassLoader pluginClassLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader());

            // Use ServiceLoader to find all FileAction implementations
            ServiceLoader<FileAction> serviceLoader = ServiceLoader.load(FileAction.class, pluginClassLoader);

            for (FileAction plugin : serviceLoader) {
                externalPlugins.add(plugin);
                System.out.println("Loaded plugin: " + plugin.getActionName());
            }

        } catch (Exception e) {
            System.err.println("Error loading external plugins: " + e.getMessage());
            e.printStackTrace();
        }

        return externalPlugins;
    }
}
