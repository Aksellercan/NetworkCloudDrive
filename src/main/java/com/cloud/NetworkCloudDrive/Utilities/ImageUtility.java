package com.cloud.NetworkCloudDrive.Utilities;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Component
public class ImageUtility {
    private final int portraitWidth = 100;
    private final int portraitHeight = 200;
    private final int landscapeWidth = 100;
    private final int landscapeHeight = 100;
    private final PathUtility pathUtility;


    public ImageUtility(PathUtility pathUtility) {
        this.pathUtility = pathUtility;
    }

    public int[] getPortraitThumbnailDimensions(Path image) throws IOException {
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
}
