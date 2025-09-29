package com.yumyumcoach.listener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import com.yumyumcoach.config.DataStore;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Path dataDirectory = resolveDataDirectory(context);
        DataStore.getInstance().initialize(dataDirectory.toString());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DataStore store = DataStore.getInstance();
        store.saveAccounts();
        store.saveProfiles();
        store.saveDiet();
        store.savePosts();
        store.saveFollows();
        store.saveChallenges();
        store.saveExercise();
        store.saveParticipants();
    }

    private Path resolveDataDirectory(ServletContext context) {
        String realPath = context.getRealPath("/WEB-INF/data");
        if (realPath != null) {
            Path path = Path.of(realPath);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to prepare data directory", e);
            }
            return path;
        }
        return createTempDirectory(context);
    }

    private Path createTempDirectory(ServletContext context) {
        try {
            Path tempDir = Files.createTempDirectory("yumyum-data");
            copyInitialData(context, tempDir);
            return tempDir;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temporary data directory", e);
        }
    }

    private void copyInitialData(ServletContext context, Path targetDir) throws IOException {
        Set<String> resources = context.getResourcePaths("/WEB-INF/data/");
        if (resources == null) {
            return;
        }
        for (String resource : resources) {
            if (resource.endsWith("/")) {
                continue;
            }
            try (InputStream input = context.getResourceAsStream(resource)) {
                if (input == null) {
                    continue;
                }
                String fileName = resource.substring(resource.lastIndexOf('/') + 1);
                Files.copy(input, targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
