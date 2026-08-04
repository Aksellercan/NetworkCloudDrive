package com.cloud.NetworkCloudDrive.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class EncodingUtility {
    private final Logger logger = LoggerFactory.getLogger(EncodingUtility.class);

    public EncodingUtility() {
    }

    /**
     * Encode User folder using BASE32
     *
     * @param userId   currently logged-in user's ID
     * @param username currently logged-in user's name
     * @param mail     currently logged-in user's mail
     * @return BASE32 encoded user folder name
     */
    public String encodeBase32UserFolderName(long userId, String username, String mail) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((userId + ":" + username + ":" + mail).getBytes());
    }

    /**
     * Decode BASE32 encoded file/folder names
     *
     * @param base32String BASE32 encoded file/folder name
     * @return decoded BASE32 string
     */
    public String decodeBase32StringNoPadding(String base32String) {
        return new String(Base64.getUrlDecoder().decode(base32String.getBytes()));
    }

    /**
     * Decode BASE32 encoded file/folder names.
     *
     * @param base32String BASE32 encoded file/folder name
     * @return decoded BASE32 string split by ":". Split index 0 is file/folder ID, 1 is file/folder name and 2 is for userID
     */
    public String[] decodedBase32SplitArray(String base32String) {
        return decodeBase32StringNoPadding(base32String).split(":");
    }

    public long getMetadataIDFromEncodedBase32(String base32String) {
        return Long.parseLong(decodedBase32SplitArray(base32String)[0]);
    }

    public boolean isEncodedStringUserDirectory(String encodedString) {
        try {
            long checkLastIndexType /* should be long */ = Long.parseLong(decodedBase32SplitArray(encodedString)[2]);
            //if successful parse MOST likely its not user directory
            logger.warn("Most likely NOT user directory");
            return false;
        } catch (/* throws */ NumberFormatException e) {
            logger.warn("Most likely user directory");
            return true;
        }
    }

    /**
     * Encode Folder names in BASE32
     *
     * @param folderId   folder's ID
     * @param folderName folder's name
     * @param userId     currently logged-in user's ID
     * @return BASE32 encoded Folder name
     */
    public String encodeBase32FolderName(long folderId, String folderName, long userId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((folderId + ":" + folderName + ":" + userId).getBytes());
    }

    /**
     * Encode File names in BASE32
     *
     * @param fileId   file's ID
     * @param fileName file's name
     * @param userId   currently logged-in user's ID
     * @return BASE32 encoded File name
     */
    public String encodeBase32FileName(long fileId, String fileName, long userId) {
        return encodeBase32FolderName(fileId, fileName, userId);
    }

    public boolean isBase32Decodable(String name) {
        try {
            String tryDecoding = decodeBase32StringNoPadding(name);
            long tryIdParse = Long.parseLong(tryDecoding.split(":")[0]);
            logger.info("trial of id parsing {} from {}", tryIdParse, tryDecoding);
            return !tryDecoding.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to parse concluding as not BASE32 for string {} Ex. {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Hashes string in given algorithms
     *
     * @param originalString string to hash
     * @param algorithm      algorithm of hash (Ex. SHA-256, SHA-512)
     * @return hashed string
     * @throws NoSuchAlgorithmException Invalid algorithm
     */
    public String hashString(String originalString, String algorithm) throws NoSuchAlgorithmException {
        // Source - https://stackoverflow.com/a/5531479
        // Posted by Jon Skeet, modified by community. See post 'Timeline' for change history
        // Retrieved 2025-12-22, License - CC BY-SA 4.0
        if (originalString == null) throw new NullPointerException("String to hash is null");
        if (algorithm == null) throw new NullPointerException("No hash algorithm given");
        return new String(MessageDigest.getInstance(algorithm).digest(originalString.getBytes(StandardCharsets.UTF_8)));
    }
}
