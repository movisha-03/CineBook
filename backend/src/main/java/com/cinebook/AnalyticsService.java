
package com.cinebook;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class AnalyticsService {

    private final Environment environment;

    public AnalyticsService(Environment environment) {
        this.environment = environment;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void refreshAnalytics(BookingCompletedEvent event) {
        try {
            Path backendDir = Path.of(System.getProperty("user.dir"));
            Path projectDir = backendDir.getParent();

            Path script = projectDir.resolve("analytics/analysis.py");
            Path outputDir = projectDir.resolve("analytics/output");
            Path websiteDir = backendDir.resolve(
                "src/main/resources/static/analytics"
            );

            ProcessBuilder builder = new ProcessBuilder(
                "py", script.toString()
            );

            builder.directory(projectDir.toFile());
            builder.redirectErrorStream(true);

            var env = builder.environment();
            env.put("DB_HOST", environment.getProperty("DB_HOST", "localhost"));
            env.put("DB_PORT", environment.getProperty("DB_PORT", "3306"));
            env.put("DB_NAME", environment.getProperty("DB_NAME", "cinebook_db"));
            env.put("DB_USERNAME", environment.getProperty("DB_USERNAME", "root"));

            String password = environment.getProperty("spring.datasource.password");
            if (password != null) {
                env.put("DB_PASSWORD", password);
            }

            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[ANALYTICS] " + line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Analytics script failed. Exit code: " + exitCode);
                return;
            }

            Files.createDirectories(websiteDir);

            String[] charts = {
                "revenue_by_movie.png",
                "occupancy_trend.png",
                "movie_popularity.png"
            };

            for (String chart : charts) {
                Files.copy(
                    outputDir.resolve(chart),
                    websiteDir.resolve(chart),
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.out.println("Analytics charts updated successfully!");

        } catch (Exception e) {
            System.err.println("Analytics refresh failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
