package com.cloud.NetworkCloudDrive.Utilities;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ImageUtility {
    private final UserUtility userUtility;
    private int portraitWidth = 50;
    private int portraitHeight = 100;
    private int landscapeWidth = 100;
    private int landscapeHeight = 100;
    private final PathUtility pathUtility;

    public ImageUtility(PathUtility pathUtility, UserUtility userUtility) {
        this.pathUtility = pathUtility;
        this.userUtility = userUtility;
    }

    public int[] getThumbnailDimensions(Path image) throws IOException {
        if (isPortrait(image)) {
            return new int[] {portraitWidth, portraitHeight};
        }
        return new int[] {landscapeWidth, landscapeHeight};
    }

    public boolean isPortrait(Path imagePath) throws IOException {
        int[] dimensions = getImageDimensions(imagePath);
        return isPortrait(dimensions[0], dimensions[1]);
    }

    public boolean isPortrait(int width, int height) {
        return height > width;
    }

    public int[] getImageDimensions(Path imagePath) throws IOException {
        BufferedImage image = convertPathToBufferedImage(imagePath);
        return new int[]{image.getWidth(), image.getHeight()};
    }

    public BufferedImage convertPathToBufferedImage(Path path) throws IOException {
        return ImageIO.read(Path.of(pathUtility.getBasePathToString(), path.toString()).toFile());
    }

    public int getPortraitWidth() {
        return portraitWidth;
    }

    public Path getThumbnailPath(boolean isPortrait) throws IOException {
        return getSizeFolder(userUtility.returnUserFolderasPath().toString(), isPortrait);
    }

    public Path getSizeFolder(String userFolder, boolean isPortrait) {
        Path portraitThumbnailsFolder = Path.of(userFolder, ".thumbnails", "portrait");
        Path horizontalThumbnailsFolder = Path.of(userFolder, ".thumbnails", "horizontal");
        return isPortrait ? portraitThumbnailsFolder : horizontalThumbnailsFolder;
    }

    public void createThumbnailDirectories(Path path) throws IOException {
        Path thumbnailsFolder = Path.of(path.toString(), ".thumbnails");
        if (!Files.exists(thumbnailsFolder))
            Files.createDirectory(thumbnailsFolder);
        // create subfolders portrait/horizontal
        Path portraitThumbnailsFolder = Path.of(path.toString(), ".thumbnails", "portrait");
        if (!Files.exists(portraitThumbnailsFolder))
            Files.createDirectory(portraitThumbnailsFolder);
        Path horizontalThumbnailsFolder = Path.of(path.toString(), ".thumbnails", "horizontal");
        if (!Files.exists(horizontalThumbnailsFolder))
            Files.createDirectory(horizontalThumbnailsFolder);
    }


    public void setPortraitWidth(int portraitWidth) {
        this.portraitWidth = portraitWidth;
    }

    public int getPortraitHeight() {
        return portraitHeight;
    }

    public void setPortraitHeight(int portraitHeight) {
        this.portraitHeight = portraitHeight;
    }

    public int getLandscapeWidth() {
        return landscapeWidth;
    }

    public void setLandscapeWidth(int landscapeWidth) {
        this.landscapeWidth = landscapeWidth;
    }

    public int getLandscapeHeight() {
        return landscapeHeight;
    }

    public void setLandscapeHeight(int landscapeHeight) {
        this.landscapeHeight = landscapeHeight;
    }
}
