package com.cloud.NetworkCloudDrive.Utilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUtilityTest {

    @Mock
    private PathUtility pathUtility;
    @Mock
    private UserUtility userUtility;

    private ImageUtility imageUtility;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageUtility = new ImageUtility(pathUtility, userUtility);
    }

    @Test
    void isPortrait_heightGreaterThanWidth_ReturnsTrue() {
        assertTrue(imageUtility.isPortrait(100, 200));
    }

    @Test
    void isPortrait_widthGreaterThanHeight_ReturnsFalse() {
        assertFalse(imageUtility.isPortrait(200, 100));
    }

    @Test
    void isPortrait_equalDimensions_ReturnsFalse() {
        assertFalse(imageUtility.isPortrait(100, 100));
    }

    @Test
    void isPortrait_zeroWidth_ReturnsTrue() {
        assertTrue(imageUtility.isPortrait(0, 100));
    }

    @Test
    void getSizeFolder_portrait_ReturnsPortraitPath() {
        Path result = imageUtility.getSizeFolder("/user/folder", true);
        assertEquals(Path.of("/user/folder", ".thumbnails", "portrait"), result);
    }

    @Test
    void getSizeFolder_landscape_ReturnsHorizontalPath() {
        Path result = imageUtility.getSizeFolder("/user/folder", false);
        assertEquals(Path.of("/user/folder", ".thumbnails", "horizontal"), result);
    }

    @Test
    void defaultDimensions_areCorrect() {
        assertEquals(50, imageUtility.getPortraitWidth());
        assertEquals(100, imageUtility.getPortraitHeight());
        assertEquals(100, imageUtility.getLandscapeWidth());
        assertEquals(100, imageUtility.getLandscapeHeight());
    }

    @Test
    void setDimensions_updatesCorrectly() {
        imageUtility.setPortraitWidth(80);
        imageUtility.setPortraitHeight(160);
        imageUtility.setLandscapeWidth(120);
        imageUtility.setLandscapeHeight(90);

        assertEquals(80, imageUtility.getPortraitWidth());
        assertEquals(160, imageUtility.getPortraitHeight());
        assertEquals(120, imageUtility.getLandscapeWidth());
        assertEquals(90, imageUtility.getLandscapeHeight());
    }

    @Test
    void createThumbnailDirectories_CreatesSubfolders() throws Exception {
        imageUtility.createThumbnailDirectories(tempDir);

        assertTrue(Files.exists(tempDir.resolve(".thumbnails")));
        assertTrue(Files.exists(tempDir.resolve(".thumbnails").resolve("portrait")));
        assertTrue(Files.exists(tempDir.resolve(".thumbnails").resolve("horizontal")));
    }

    @Test
    void createThumbnailDirectories_WhenAlreadyExists_DoesNotThrow() throws Exception {
        Files.createDirectories(tempDir.resolve(".thumbnails").resolve("portrait"));
        Files.createDirectories(tempDir.resolve(".thumbnails").resolve("horizontal"));

        imageUtility.createThumbnailDirectories(tempDir);

        assertTrue(Files.exists(tempDir.resolve(".thumbnails")));
    }

    @Test
    void getImageDimensions_ReturnsCorrectDimensions() throws Exception {
        BufferedImage img = new BufferedImage(80, 120, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("test.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        int[] dimensions = imageUtility.getImageDimensions(tempDir.relativize(imageFile));

        assertEquals(80, dimensions[0]);
        assertEquals(120, dimensions[1]);
    }

    @Test
    void isPortrait_WithPath_ReturnsTrueForPortraitImage() throws Exception {
        BufferedImage img = new BufferedImage(50, 100, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("portrait.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        boolean result = imageUtility.isPortrait(tempDir.relativize(imageFile));

        assertTrue(result);
    }

    @Test
    void isPortrait_WithPath_ReturnsFalseForLandscapeImage() throws Exception {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("landscape.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        boolean result = imageUtility.isPortrait(tempDir.relativize(imageFile));

        assertFalse(result);
    }

    @Test
    void getThumbnailDimensions_PortraitImage_ReturnsPortraitDimensions() throws Exception {
        BufferedImage img = new BufferedImage(60, 90, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("img.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        int[] dims = imageUtility.getThumbnailDimensions(tempDir.relativize(imageFile));

        assertEquals(50, dims[0]);
        assertEquals(100, dims[1]);
    }

    @Test
    void getThumbnailDimensions_LandscapeImage_ReturnsLandscapeDimensions() throws Exception {
        BufferedImage img = new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("img.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        int[] dims = imageUtility.getThumbnailDimensions(tempDir.relativize(imageFile));

        assertEquals(100, dims[0]);
        assertEquals(100, dims[1]);
    }

    @Test
    void convertPathToBufferedImage_ReturnsImage() throws Exception {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Path imageFile = tempDir.resolve("small.png");
        ImageIO.write(img, "png", imageFile.toFile());

        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());

        BufferedImage result = imageUtility.convertPathToBufferedImage(tempDir.relativize(imageFile));

        assertNotNull(result);
        assertEquals(30, result.getWidth());
        assertEquals(30, result.getHeight());
    }
}
