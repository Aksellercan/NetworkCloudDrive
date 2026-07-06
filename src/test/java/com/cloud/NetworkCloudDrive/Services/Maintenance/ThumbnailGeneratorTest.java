package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.TestUtility;
import com.cloud.NetworkCloudDrive.Utilities.ImageUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.scheduling.annotation.Async;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class ThumbnailGeneratorTest {
    Logger logger = LoggerFactory.getLogger(ThumbnailGeneratorTest.class);

    @TempDir
    Path tempRoot;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private SQLiteDAO sqLiteDAO;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ImageUtility imageUtility;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private EncodingUtility encodingUtility;

    @Autowired
    private UserUtility userUtility;

    private Path userFolder;
    private String originalBasePath;
    private long userId;

    @BeforeEach
    void setUp() throws IOException {
        originalBasePath = fileStorageProperties.getBasePath();
        fileStorageProperties.setBasePath(tempRoot.toString() + "/");

        UserEntity user = new UserEntity("thumb_user", "thumb@test.com", "password", UserRole.GUEST);
        user = sqLiteDAO.saveUser(user);
        userId = user.getId();

        userSession.setId(user.getId());
        userSession.setName(user.getName());
        userSession.setMail(user.getMail());
        userSession.setRole(user.getRole());

        userFolder = userUtility.returnUserFolderasPath();
    }

    @AfterEach
    void tearDown() {
        fileStorageProperties.setBasePath(originalBasePath);
    }

    @Test
    void nasaApodImage_thumbnailGenerated() throws IOException, ExecutionException, InterruptedException {
        Optional<BufferedImage> nasaImage = TestUtility.fetchNasaApodImage();
        if (nasaImage.isEmpty()) {
            return;
        }
        assertThumbnailGenerated(nasaImage.get());
    }

    @Test
    void portraitGradient_thumbnailGenerated() throws IOException, ExecutionException, InterruptedException {
        BufferedImage img = TestUtility.gradient(100, 200, Color.RED, Color.BLUE);
        assertThumbnailGenerated(img);
    }

    @Test
    void landscapeGradient_thumbnailGenerated() throws IOException, ExecutionException, InterruptedException {
        BufferedImage img = TestUtility.gradient(200, 100, Color.BLUE, Color.GREEN);
        assertThumbnailGenerated(img);
    }

    @Test
    void createAndSaveThumbnailDefaultSettings_IsAnnotatedWithAsync() throws Exception {
        Method method = ThumbnailService.class.getMethod(
                "createAndSaveThumbnailDefaultSettings", Path.class, String.class, long.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void createAndSaveThumbnailDefaultSettings_ReturnsCompletableFuture() throws Exception {
        Method method = ThumbnailService.class.getMethod(
                "createAndSaveThumbnailDefaultSettings", Path.class, String.class, long.class);
        assertEquals(CompletableFuture.class, method.getReturnType());
    }

    @Test
    void squareGradient_thumbnailGenerated() throws IOException, ExecutionException, InterruptedException {
        BufferedImage img = TestUtility.gradient(100, 100, Color.GREEN, Color.RED);
        assertThumbnailGenerated(img);
    }

    private void assertThumbnailGenerated(BufferedImage source) throws IOException, ExecutionException, InterruptedException {
        int width = source.getWidth();
        int height = source.getHeight();
        boolean expectedPortrait = height > width;

        String fileName = "test_image.jpg";
        Path imageFile = userFolder.resolve(fileName);
        ImageIO.write(source, "jpg", imageFile.toFile());

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, userId, "image/jpeg", (long) width * height * 3);
        fileMetadata = sqLiteDAO.saveFile(fileMetadata);

        String encodedFileName = encodingUtility.encodeBase32FileName(fileMetadata.getId(), fileName, userId);
        Path encodedPath = userFolder.resolve(encodedFileName);
        Files.move(imageFile, encodedPath);

        BufferedImage check = ImageIO.read(encodedPath.toFile());
        assertNotNull(check, "Saved encoded image should be readable");
        assertEquals(width, check.getWidth(), "Encoded image width should match source");
        assertEquals(height, check.getHeight(), "Encoded image height should match source");

        Path relativePath = Path.of(userFolder.getFileName().toString(), encodedFileName);

        logger.info("Testing for {}", fileMetadata.getId());
        ThumbnailMetadata result = thumbnailService.createAndSaveThumbnailDefaultSettings(relativePath, encodedFileName, fileMetadata.getId()).get();

        assertNotNull(result);
        assertTrue(result.getId() > 0);
        assertEquals(expectedPortrait, result.isPortrait(), "Orientation flag should match image dimensions");
        assertTrue(result.getFileName().endsWith("_thumbnail.jpg"));
        assertTrue(result.getFileName().startsWith(encodedFileName));

        Path expectedThumbnailFile = imageUtility.getSizeFolder(userFolder.toString(), expectedPortrait)
                .resolve(result.getFileName());
        assertTrue(Files.exists(expectedThumbnailFile),
                "Thumbnail file should exist at " + expectedThumbnailFile);

        BufferedImage thumbImg = ImageIO.read(expectedThumbnailFile.toFile());
        assertNotNull(thumbImg, "Thumbnail should be a readable image");
        double scale = Math.min(
                (double) (expectedPortrait ? imageUtility.getPortraitWidth() : imageUtility.getLandscapeWidth()) / width,
                (double) (expectedPortrait ? imageUtility.getPortraitHeight() : imageUtility.getLandscapeHeight()) / height);
        int expectedThumbWidth = (int) Math.round(width * scale);
        int expectedThumbHeight = (int) Math.round(height * scale);
        assertEquals(expectedThumbWidth, thumbImg.getWidth());
        assertEquals(expectedThumbHeight, thumbImg.getHeight());
    }
}
