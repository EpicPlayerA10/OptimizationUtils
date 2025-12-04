package com.epicplayera10.optimizationutils.updatechecker;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker {

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/optimizationutils/version";
    private static final String PROJECT_SLUG = "optimizationutils";

    private String latestVersion;
    private boolean updateAvailable = false;

    public void checkForUpdates() {
        OptimizationUtils.instance().getServer().getScheduler().runTaskAsynchronously(OptimizationUtils.instance(), () -> {
            try {
                URL url = new URL(MODRINTH_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "OptimizationUtils/" + OptimizationUtils.instance().getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    parseVersionResponse(response.toString());
                } else {
                    OptimizationUtils.instance().getLogger().log(Level.WARNING, "Failed to check for updates. Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                OptimizationUtils.instance().getLogger().log(Level.WARNING, "Failed to check for updates from Modrinth", e);
            }
        });
    }

    private void parseVersionResponse(String json) {
        try {
            JsonArray versions = JsonParser.parseString(json).getAsJsonArray();

            if (versions.size() == 0) {
                return;
            }

            // Get the latest version (first in the array)
            JsonObject latestVersionObj = versions.get(0).getAsJsonObject();
            this.latestVersion = latestVersionObj.get("version_number").getAsString();

            String currentVersion = OptimizationUtils.instance().getDescription().getVersion().replace("-SNAPSHOT", "");

            if (!currentVersion.equals(latestVersion)) {
                updateAvailable = true;
                notifyConsole();
            } else {
                OptimizationUtils.instance().getLogger().info("You are running the latest version of OptimizationUtils!");
            }

        } catch (Exception e) {
            OptimizationUtils.instance().getLogger().log(Level.WARNING, "Failed to parse version information", e);
        }
    }

    private void notifyConsole() {
        OptimizationUtils.instance().getLogger().warning("==========================================");
        OptimizationUtils.instance().getLogger().warning("A new version of OptimizationUtils is available!");
        OptimizationUtils.instance().getLogger().warning("Current version: " + OptimizationUtils.instance().getDescription().getVersion());
        OptimizationUtils.instance().getLogger().warning("Latest version: " + latestVersion);
        OptimizationUtils.instance().getLogger().warning("Download: https://modrinth.com/plugin/optimizationutils");
        OptimizationUtils.instance().getLogger().warning("==========================================");
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}