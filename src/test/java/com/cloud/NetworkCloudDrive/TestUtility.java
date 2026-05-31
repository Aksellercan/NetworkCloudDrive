package com.cloud.NetworkCloudDrive;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class TestUtility {

    private static final String NASA_APOD_URL =
            "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches today's NASA Astronomy Picture of the Day (APOD) as a BufferedImage.
     * Returns empty if the APOD is a video, the request fails, or the image can't be read.
     */
    public static Optional<BufferedImage> fetchNasaApodImage() {
        try {
            String json = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(NASA_APOD_URL))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();

            JsonNode root = objectMapper.readTree(json);
            String imageUrl = root.get("url").asString();
            if (!imageUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$")) {
                return Optional.empty();
            }

            byte[] imageBytes = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(imageUrl))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            ).body();

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return Optional.ofNullable(img);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Creates a smooth gradient BufferedImage of the given dimensions.
     */
    public static BufferedImage gradient(int width, int height, Color from, Color to) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, from, width, height, to));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
