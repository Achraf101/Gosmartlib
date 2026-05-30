package be.ap.backend.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.*;

/**
 * Initialiseert standaard cover-bestanden bij startup van de applicatie.
 *
 * <p>
 * Bij het opstarten worden resources uit de classpath gekopieerd naar de
 * geconfigureerde uploadmap indien ze nog niet bestaan.
 * </p>
 *
 * <p>
 * Deze operatie is idempotent en overschrijft bestaande bestanden niet.
 * </p>
 */
@Component
public class CoverInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CoverInitializer.class);
    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * Kopieert default cover-bestanden van de classpath naar de upload directory.
     *
     * <p>
     * Wordt automatisch uitgevoerd bij applicatiestart via
     * {@link CommandLineRunner}.
     * </p>
     *
     * @throws Exception indien het aanmaken van directories of kopiëren van
     *                   bestanden faalt
     */
    @Override
    public void run(String... args) throws Exception {
        Path targetDir = Paths.get(uploadDir, "cover");

        Files.createDirectories(targetDir);

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:covers/*");
        log.debug("Found {} cover resources", resources.length);

        for (Resource res : resources) {
            if (!res.exists() || res.getFilename() == null)
                continue;
            Path targetFile = targetDir.resolve(res.getFilename());

            if (!Files.exists(targetFile)) {
                Files.copy(res.getInputStream(), targetFile);
            }
        }

        log.info("Cover files copied to {}", targetDir);
    }
}