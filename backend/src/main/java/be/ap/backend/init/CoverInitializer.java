package be.ap.backend.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.*;

@Component
public class CoverInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CoverInitializer.class);
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void run(String... args) throws Exception {
        Path targetDir = Paths.get(uploadDir, "cover");

        Files.createDirectories(targetDir);

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:covers/*");

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