package com.cloud.NetworkCloudDrive.Security;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EncodingUtilityTest {

    @Mock
    private SQLiteDAO sqLiteDAO;

    private EncodingUtility encodingUtility;

    @BeforeEach
    void setUp() {
        encodingUtility = new EncodingUtility();
    }

    @Test
    void encodeBase32UserFolderName_And_Decode_Roundtrip() {
        long id = 1L;
        String name = "testuser";
        String mail = "test@example.com";

        String encoded = encodingUtility.encodeBase32UserFolderName(id, name, mail);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());

        String decoded = encodingUtility.decodeBase32StringNoPadding(encoded);
        String[] parts = decoded.split(":");
        assertEquals("1", parts[0]);
        assertEquals("testuser", parts[1]);
        assertEquals("test@example.com", parts[2]);
    }

    @Test
    void encodeBase32FolderName_And_Decode_Roundtrip() {
        long folderId = 42L;
        String folderName = "myFolder";
        long userId = 7L;

        String encoded = encodingUtility.encodeBase32FolderName(folderId, folderName, userId);
        assertNotNull(encoded);

        String[] decoded = encodingUtility.decodedBase32SplitArray(encoded);
        assertEquals("42", decoded[0]);
        assertEquals("myFolder", decoded[1]);
        assertEquals("7", decoded[2]);
    }

    @Test
    void encodeBase32FileName_ProducesSameAsFolderName() {
        String fileEncoded = encodingUtility.encodeBase32FileName(10L, "file.txt", 3L);
        String folderEncoded = encodingUtility.encodeBase32FolderName(10L, "file.txt", 3L);
        assertEquals(folderEncoded, fileEncoded);
    }

    @Test
    void decodedBase32SplitArray_ReturnsCorrectParts() {
        String encoded = encodingUtility.encodeBase32FolderName(99L, "document.pdf", 5L);
        String[] parts = encodingUtility.decodedBase32SplitArray(encoded);
        assertEquals(3, parts.length);
        assertEquals("99", parts[0]);
        assertEquals("document.pdf", parts[1]);
        assertEquals("5", parts[2]);
    }

    @Test
    void getMetadataIDFromEncodedBase32_ReturnsCorrectId() {
        String encoded = encodingUtility.encodeBase32FolderName(77L, "photo.jpg", 2L);
        long id = encodingUtility.getMetadataIDFromEncodedBase32(encoded);
        assertEquals(77L, id);
    }

    @Test
    void hashString_SHA256_ReturnsNonNull() throws NoSuchAlgorithmException {
        String hash = encodingUtility.hashString("hello", "SHA-256");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashString_SHA512_ReturnsNonNull() throws NoSuchAlgorithmException {
        String hash = encodingUtility.hashString("hello", "SHA-512");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashString_DifferentInputs_ProduceDifferentHashes() throws NoSuchAlgorithmException {
        String hash1 = encodingUtility.hashString("hello", "SHA-256");
        String hash2 = encodingUtility.hashString("world", "SHA-256");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void hashString_NullInput_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> encodingUtility.hashString(null, "SHA-256"));
    }

    @Test
    void hashString_NullAlgorithm_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> encodingUtility.hashString("test", null));
    }

    @Test
    void hashString_InvalidAlgorithm_ThrowsNoSuchAlgorithmException() {
        assertThrows(NoSuchAlgorithmException.class, () -> encodingUtility.hashString("test", "INVALID_ALGO"));
    }

    @Test
    void isBase32Decodable_WithValidEncodedString_ReturnsTrue() {
        String encoded = encodingUtility.encodeBase32FolderName(1L, "test", 1L);
        assertTrue(encodingUtility.isBase32Decodable(encoded));
    }

    @Test
    void isBase32Decodable_WithInvalidString_ReturnsFalse() {
        assertFalse(encodingUtility.isBase32Decodable("not-encoded!!!"));
    }

    @Test
    void isBase32Decodable_WithEmptyString_ReturnsFalse() {
        assertFalse(encodingUtility.isBase32Decodable(""));
    }

    @Test
    void isEncodedStringUserDirectory_WithUserFolderFormat_ReturnsTrue() {
        String encoded = encodingUtility.encodeBase32UserFolderName(1L, "user", "mail@test.com");
        assertTrue(encodingUtility.isEncodedStringUserDirectory(encoded));
    }

    @Test
    void isEncodedStringUserDirectory_WithFileFolderFormat_ReturnsFalse() {
        String encoded = encodingUtility.encodeBase32FolderName(5L, "file.txt", 1L);
        assertFalse(encodingUtility.isEncodedStringUserDirectory(encoded));
    }

    @Test
    void getFolderMetadataFromEncoding_CallsDAO() throws Exception {
        String encoded = encodingUtility.encodeBase32FolderName(10L, "folder", 1L);
        FolderMetadata expected = new FolderMetadata();
        expected.setId(10L);
        expected.setName("folder_encoded");
        expected.setUserid(1L);

        sqLiteDAO.getFolderMetadataFromEncoding(encoded, 1L);
    }
}
