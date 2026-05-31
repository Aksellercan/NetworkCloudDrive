package com.cloud.NetworkCloudDrive.Utilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageUtilityTest {

    @Mock
    private PathUtility pathUtility;
    @Mock
    private UserUtility userUtility;

    private ImageUtility imageUtility;

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
}
