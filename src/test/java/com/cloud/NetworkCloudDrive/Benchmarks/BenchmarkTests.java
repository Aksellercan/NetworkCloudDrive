package com.cloud.NetworkCloudDrive.Benchmarks;

import com.cloud.NetworkCloudDrive.Models.Domain.Benchmark;
import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.Domain.ScanResults;
import net.coobird.thumbnailator.Thumbnailator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTests {

    @TempDir
    Path tempDir;

    @Test
    void benchmark_RecordsStartTime() {
        Benchmark benchmark = new Benchmark();
        assertTrue(benchmark.getTimeTaken() > 0);
    }

    @Test
    void benchmark_StopTimerReturnsNonNegativeElapsed() throws Exception {
        Benchmark benchmark = new Benchmark();
        Thread.sleep(5);
        long elapsed = benchmark.stopTimerAndGetTimeTaken();
        assertTrue(elapsed >= 5);
    }

    @Test
    void benchmark_ElapsedTimeAdvances() throws Exception {
        Benchmark benchmark = new Benchmark();
        long before = benchmark.getTimeTaken();
        Thread.sleep(10);
        long after = System.currentTimeMillis();
        assertTrue(benchmark.getTimeTaken() >= before);
        assertTrue(after >= before);
    }

    @Test
    void deletionResults_CountersAndToString() {
        DeletionResults dr = new DeletionResults();
        assertEquals(0, dr.getSuccessful_removals());
        assertEquals(0, dr.getRemoval_failures());

        dr.incrementSuccessfulRemovals();
        dr.incrementSuccessfulRemovals();
        dr.incrementRemovalFailures();

        assertEquals(2, dr.getSuccessful_removals());
        assertEquals(1, dr.getRemoval_failures());

        String s = dr.toString();
        assertTrue(s.contains("Successfully Removed Files: 2"));
        assertTrue(s.contains("Files Failed to Remove: 1"));
        assertTrue(s.contains("Time Taken:"));
    }

    @Test
    void scanResults_CountersAndToString() {
        ScanResults sr = new ScanResults();
        assertEquals(0, sr.getDiscoveredFiles());
        assertEquals(0, sr.getDiscoveredFolders());
        assertEquals(0, sr.getCreatedFiles());
        assertEquals(0, sr.getCreatedFolders());

        sr.incrementDiscoveredFileCount();
        sr.incrementDiscoveredFolderCount();
        sr.incrementCreatedFileCount();
        sr.incrementCreatedFolderCount();
        sr.incrementDiscoveredFileCount();

        assertEquals(2, sr.getDiscoveredFiles());
        assertEquals(1, sr.getDiscoveredFolders());
        assertEquals(1, sr.getCreatedFiles());
        assertEquals(1, sr.getCreatedFolders());

        String s = sr.toString();
        assertTrue(s.contains("Discovered Folders: 1"));
        assertTrue(s.contains("Discovered Files: 2"));
        assertTrue(s.contains("Created Files: 1"));
        assertTrue(s.contains("Created Folders: 1"));
        assertTrue(s.contains("Time Taken:"));
    }

    @Test
    void fileCreation_SmallFile_CompletesUnderThreshold() throws Exception {
        byte[] data = "hello world".getBytes();
        Path file = tempDir.resolve("small.txt");

        long start = System.nanoTime();
        Files.write(file, data);
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(file));
        assertEquals(data.length, Files.size(file));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void fileCreation_LargeFile_CompletesUnderThreshold() throws Exception {
        byte[] data = new byte[1_048_576];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        Path file = tempDir.resolve("large.bin");

        long start = System.nanoTime();
        Files.write(file, data);
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(file));
        assertEquals(1_048_576, Files.size(file));
        assertTrue(elapsed < 10_000_000_000L);
    }

    @Test
    void streamCopy_UploadSimulation_CompletesUnderThreshold() throws Exception {
        byte[] data = new byte[524_288];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        InputStream inputStream = new ByteArrayInputStream(data);
        Path dest = tempDir.resolve("uploaded.bin");

        long start = System.nanoTime();
        StreamUtils.copy(inputStream, Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW));
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(dest));
        assertEquals(524_288, Files.size(dest));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void fileRead_DownloadSimulation_CompletesUnderThreshold() throws Exception {
        byte[] data = new byte[524_288];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        Path file = tempDir.resolve("download.dat");
        Files.write(file, data);

        long start = System.nanoTime();
        byte[] readBack = Files.readAllBytes(file);
        long elapsed = System.nanoTime() - start;

        assertArrayEquals(data, readBack);
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void streamRead_DownloadSimulation_CompletesUnderThreshold() throws Exception {
        byte[] data = new byte[262_144];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        Path file = tempDir.resolve("streamfile.dat");
        Files.write(file, data);

        long start = System.nanoTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = Files.newInputStream(file)) {
            StreamUtils.copy(is, baos);
        }
        long elapsed = System.nanoTime() - start;

        assertArrayEquals(data, baos.toByteArray());
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void directoryCreation_DeepTree_CompletesUnderThreshold() throws Exception {
        long start = System.nanoTime();
        Path deep = tempDir.resolve("a").resolve("b").resolve("c").resolve("d").resolve("e");
        Files.createDirectories(deep);
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(deep));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void directoryListing_WithManyFiles_CompletesUnderThreshold() throws Exception {
        int fileCount = 100;
        for (int i = 0; i < fileCount; i++) {
            Files.writeString(tempDir.resolve("file_" + i + ".txt"), "data");
        }

        long start = System.nanoTime();
        try (Stream<Path> stream = Files.list(tempDir)) {
            List<Path> files = stream.toList();
            assertEquals(fileCount, files.size());
        }
        long elapsed = System.nanoTime() - start;

        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void directoryWalk_DeepTree_CompletesUnderThreshold() throws Exception {
        Path sub1 = Files.createDirectories(tempDir.resolve("sub1").resolve("sub2"));
        Files.writeString(sub1.resolve("a.txt"), "a");
        Files.writeString(sub1.resolve("b.txt"), "b");
        Files.writeString(tempDir.resolve("c.txt"), "c");

        long start = System.nanoTime();
        try (Stream<Path> stream = Files.walk(tempDir.resolve("sub1"))) {
            assertEquals(4, stream.count());
        }
        long elapsed = System.nanoTime() - start;

        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void fileDeletion_SingleFile_CompletesUnderThreshold() throws Exception {
        Path file = Files.writeString(tempDir.resolve("todelete.txt"), "data");

        long start = System.nanoTime();
        boolean deleted = Files.deleteIfExists(file);
        long elapsed = System.nanoTime() - start;

        assertTrue(deleted);
        assertTrue(Files.notExists(file));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void fileDeletion_TreeWalk_CompletesUnderThreshold() throws Exception {
        int totalFiles = 50;
        for (int i = 0; i < totalFiles; i++) {
            Files.writeString(tempDir.resolve("del_" + i + ".txt"), "data");
        }

        long start = System.nanoTime();
        try (Stream<Path> stream = Files.walk(tempDir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        }
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.notExists(tempDir.resolve("del_0.txt")));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void deleteFolders_WithDeletionResults_CountsCorrectly() throws Exception {
        Path dir = Files.createDirectory(tempDir.resolve("target"));
        Files.writeString(dir.resolve("f1.txt"), "a");
        Files.writeString(dir.resolve("f2.txt"), "b");
        Path sub = Files.createDirectory(dir.resolve("sub"));
        Files.writeString(sub.resolve("f3.txt"), "c");

        DeletionResults results = new DeletionResults();
        List<Path> tree;
        try (Stream<Path> stream = Files.walk(dir)) {
            tree = stream.sorted(Comparator.reverseOrder()).toList();
        }
        long start = System.nanoTime();
        for (Path p : tree) {
            if (Files.deleteIfExists(p)) {
                results.incrementSuccessfulRemovals();
            } else {
                results.incrementRemovalFailures();
            }
        }
        results.stopTimerAndGetTimeTaken();

        assertEquals(5, results.getSuccessful_removals());
        assertTrue(results.getTimeTaken() >= 0);
    }

    @Test
    void fileMove_AcrossDirectories_CompletesUnderThreshold() throws Exception {
        Path srcDir = Files.createDirectory(tempDir.resolve("source"));
        Path dstDir = Files.createDirectory(tempDir.resolve("dest"));
        Path file = Files.writeString(srcDir.resolve("move_me.txt"), "movable content");

        long start = System.nanoTime();
        Path moved = Files.move(file, dstDir.resolve("move_me.txt"));
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(moved));
        assertTrue(Files.notExists(file));
        assertEquals("movable content", Files.readString(moved));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void fileRename_CompletesUnderThreshold() throws Exception {
        Path file = Files.writeString(tempDir.resolve("old_name.txt"), "rename content");

        long start = System.nanoTime();
        Path renamed = Files.move(file, tempDir.resolve("new_name.txt"));
        long elapsed = System.nanoTime() - start;

        assertTrue(Files.exists(renamed));
        assertTrue(Files.notExists(file));
        assertEquals("rename content", Files.readString(renamed));
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void imageIO_WriteAndRead_CompletesUnderThreshold() throws Exception {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        img.getGraphics().fillRect(0, 0, 200, 200);
        Path file = tempDir.resolve("test_image.jpg");

        long start = System.nanoTime();
        assertTrue(ImageIO.write(img, "jpg", file.toFile()));
        long writeElapsed = System.nanoTime() - start;

        assertTrue(Files.exists(file));
        assertTrue(writeElapsed < 5_000_000_000L);

        start = System.nanoTime();
        BufferedImage readBack = ImageIO.read(file.toFile());
        long readElapsed = System.nanoTime() - start;

        assertNotNull(readBack);
        assertEquals(200, readBack.getWidth());
        assertEquals(200, readBack.getHeight());
        assertTrue(readElapsed < 5_000_000_000L);
    }

    @Test
    void thumbnailCreation_WithThumbnailator_CompletesUnderThreshold() throws Exception {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        img.getGraphics().fillRect(0, 0, 800, 600);
        Path source = tempDir.resolve("large.jpg");
        ImageIO.write(img, "jpg", source.toFile());

        long start = System.nanoTime();
        BufferedImage thumbnail = Thumbnailator.createThumbnail(source.toFile(), 100, 100);
        long elapsed = System.nanoTime() - start;

        assertNotNull(thumbnail);
        assertEquals(100, thumbnail.getWidth());
        assertEquals(75, thumbnail.getHeight());
        assertTrue(elapsed < 5_000_000_000L);
    }

    @Test
    void benchmark_UsedWithFileCreation_ProducesTiming() throws Exception {
        Benchmark benchmark = new Benchmark();
        Files.writeString(tempDir.resolve("benchmarked.txt"), "timed content");
        long elapsed = benchmark.stopTimerAndGetTimeTaken();

        assertTrue(Files.exists(tempDir.resolve("benchmarked.txt")));
        assertTrue(elapsed >= 0);
    }

    @Test
    void scanResults_UsedAfterDirectoryScan_ProducesTiming() throws Exception {
        Files.createDirectories(tempDir.resolve("scan_a").resolve("scan_b"));
        Files.writeString(tempDir.resolve("scan_a").resolve("f1.txt"), "x");
        Files.writeString(tempDir.resolve("scan_a").resolve("f2.txt"), "y");

        ScanResults scanResults = new ScanResults();
        try (Stream<Path> stream = Files.walk(tempDir)) {
            for (Path p : stream.toList()) {
                if (Files.isRegularFile(p)) {
                    scanResults.incrementDiscoveredFileCount();
                } else if (!p.equals(tempDir)) {
                    scanResults.incrementDiscoveredFolderCount();
                }
            }
        }
        scanResults.stopTimerAndGetTimeTaken();

        assertEquals(2, scanResults.getDiscoveredFiles());
        assertEquals(2, scanResults.getDiscoveredFolders());
        assertTrue(scanResults.getTimeTaken() >= 0);
    }
}
