package com.cloud.NetworkCloudDrive.Persistence;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.JdbcImpl.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.JdbcImpl.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Repositories.JdbcImpl.SQLiteThumbnailRepository;
import com.cloud.NetworkCloudDrive.Repositories.JdbcImpl.SQLiteUserEntityRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Basically DAO but for multiple types*
@Component
public class SQLiteDAO {
    private final EntityManager entityManager;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteUserEntityRepository sqLiteUserEntityRepository;
    private final SQLiteThumbnailRepository sqLiteThumbnailRepository;
    private final Logger logger = LoggerFactory.getLogger(SQLiteDAO.class);
    private final ThumbnailProperties thumbnailProperties;
    private final EncodingUtility encodingUtility;

    public SQLiteDAO(
            SQLiteFolderRepository sqLiteFolderRepository,
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteUserEntityRepository sqLiteUserEntityRepository,
            SQLiteThumbnailRepository sqLiteThumbnailRepository,
            EntityManager entityManager,
            ThumbnailProperties thumbnailProperties, EncodingUtility encodingUtility) {
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.sqLiteUserEntityRepository = sqLiteUserEntityRepository;
        this.sqLiteThumbnailRepository = sqLiteThumbnailRepository;
        this.entityManager = entityManager;
        this.thumbnailProperties = thumbnailProperties;
        this.encodingUtility = encodingUtility;
    }

    // DAO stuff

    // Get access to sqlite repositories anyway
    public SQLiteFolderRepository getSqLiteFolderRepository() {
        return sqLiteFolderRepository;
    }

    public SQLiteUserEntityRepository getSqLiteUserEntityRepository() {
        return sqLiteUserEntityRepository;
    }

    public SQLiteFileRepository getSqLiteFileRepository() {
        return sqLiteFileRepository;
    }

    public SQLiteThumbnailRepository getSqLiteThumbnailRepository() {
        return sqLiteThumbnailRepository;
    }

    // Delete
    @Transactional
    public void deleteFolder(FolderMetadata folder) {
        sqLiteFolderRepository.delete(folder);
    }

    @Transactional
    public void deleteFile(FileMetadata file) {
        sqLiteFileRepository.delete(file);
    }

    @Transactional
    public void deleteUser(UserEntity userEntity) {
        sqLiteUserEntityRepository.delete(userEntity);
    }

    @Transactional
    public void deleteThumbnail(ThumbnailMetadata thumbnail) {
        sqLiteThumbnailRepository.delete(thumbnail);
    }

    // Delete collection
    @Transactional
    public void deleteAllFolders(List<FolderMetadata> folders) {
        sqLiteFolderRepository.deleteAllInBatch(folders);
    }

    @Transactional
    public void deleteAllFiles(List<FileMetadata> files) {
        sqLiteFileRepository.deleteAllInBatch(files);
    }

    @Transactional
    public void deleteAllUsers(List<UserEntity> userEntities) {
        sqLiteUserEntityRepository.deleteAllInBatch(userEntities);
    }

    @Transactional
    public void deleteAllThumbnails(List<ThumbnailMetadata> thumbnails) {
        sqLiteThumbnailRepository.deleteAllInBatch(thumbnails);
    }

    // Add/Update
    @Transactional
    public FolderMetadata saveFolder(FolderMetadata folder) {
        return sqLiteFolderRepository.save(folder);
    }

    @Transactional
    public FileMetadata saveFile(FileMetadata file) {
        return sqLiteFileRepository.save(file);
    }

    @Transactional
    public UserEntity saveUser(UserEntity userEntity) {
        return sqLiteUserEntityRepository.save(userEntity);
    }

    @Transactional
    public ThumbnailMetadata saveThumbnail(ThumbnailMetadata thumbnail) {
        return sqLiteThumbnailRepository.save(thumbnail);
    }

    // Add/Update using collections
    @Transactional
    public List<FolderMetadata> saveAllFolders(List<FolderMetadata> folderMetadata) {
        return sqLiteFolderRepository.saveAll(folderMetadata);
    }

    @Transactional
    public List<FileMetadata> saveAllFiles(List<FileMetadata> fileMetadata) {
        return sqLiteFileRepository.saveAll(fileMetadata);
    }

    @Transactional
    public List<ThumbnailMetadata> saveAllThumbnails(List<ThumbnailMetadata> thumbnailMetadata) {
        return sqLiteThumbnailRepository.saveAll(thumbnailMetadata);
    }

    // Database service layer
    @Transactional
    public FileMetadata findFileMetadataById(long id) {
        return sqLiteFileRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<FileMetadata> searchFileMetadataByName(String name) {
        return sqLiteFileRepository.searchFileMetadataByName(name);
    }

    @Transactional
    public boolean fileMetadataByNameExists(String name) {
        return sqLiteFileRepository.existsFileMetadataByName(name);
    }

    @Transactional
    public boolean folderMetadataByNameExists(String name) {
        return sqLiteFolderRepository.existsFolderMetadataByName(name);
    }

    // to avoid putting @Transactional annotation
    @Transactional
    public void persistObjects(Object object) {
        entityManager.persist(object);
    }

    @Transactional
    public CurrentUserDTO getUserIDNameAndRoleByMail(String mail) {
        UserEntity user = findUserByMail(mail);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    private UserEntity setupExampleUser(String name, String mail) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setMail(mail);
        userEntity.setRole(null);
        userEntity.setPassword(null);
        userEntity.setId(null);
        userEntity.setLastLogin(null);
        userEntity.setRegisteredAt(null);
        return userEntity;
    }

    @Transactional
    public boolean checkIfUserExists(String name, String mail) {
        Optional<UserEntity> userOptional = sqLiteUserEntityRepository.findOne(Example.of(setupExampleUser(name, mail)));
        return userOptional.isPresent();
    }

    @Transactional
    public boolean checkIfUserExistsByMail(String mail) {
        Optional<UserEntity> userOptional = sqLiteUserEntityRepository.findByMail(mail);
        return userOptional.isPresent();
    }

    @Transactional
    public UserEntity findUserByNameAndMail(String name, String mail) {
        return sqLiteUserEntityRepository.findOne(Example.of(setupExampleUser(name, mail))).orElse(null);
    }

    @Transactional
    public UserEntity findUserByMail(String mail) {
        return sqLiteUserEntityRepository.findByMail(mail).orElse(null);
    }

    @Transactional
    public List<FolderMetadata> findAllContainingSectionOfIdPathIgnoreCase(String idPath, long userId) {
        return sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath).stream()
                .filter(f -> f.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileMetadata queryFileMetadata(long fileId, long userId) {
        return sqLiteFileRepository.findById(fileId).filter(fl -> fl.getUserid() == userId).orElse(null);
    }

    @Transactional
    public ThumbnailMetadata queryThumbnailMetadata(long thumbnailId, long userId) {
        return sqLiteThumbnailRepository.findById(thumbnailId).filter(tm -> tm.getUserId() == userId).orElse(null);
    }

    @Transactional
    public List<FileMetadata> getAllFilesBelongingToUser(long userId) {
        return sqLiteFileRepository.findAllByUserid(userId);
    }

    @Transactional
    public List<FolderMetadata> getAllFoldersBelongingToUser(long userId) {
        return sqLiteFolderRepository.findAllByUserid(userId);
    }

    @Transactional
    public List<FileListItemDTO> getAllFilesBelongingToUserAsDTO(long userId) {
        List<FileMetadata> list = sqLiteFileRepository.findAllByUserid(userId);
        List<FileListItemDTO> returnList = new ArrayList<>(); //could be LinkedList as well as it won't be modified
        for (FileMetadata fileMetadata : list) {
            FileListItemDTO fileListItemDTO = new FileListItemDTO(fileMetadata);
            if (encodingUtility.isBase32Decodable(fileMetadata.getName())) {
                fileListItemDTO.setName(encodingUtility.decodedBase32SplitArray(fileMetadata.getName())[1]);
            }
            returnList.add(fileListItemDTO);
        }
        return returnList;
    }

    @Transactional
    public List<FileListItemDTO> getAllFilesBelongingToUserAsDTOPageable(long userId, Pageable pageable) {
        // queries all folders where user id = x then orders by lastupdated descending
        Page<FileMetadata> list = sqLiteFileRepository.findAllByUseridAndLastUpdatedNotNullOrderByLastUpdatedDesc(userId, pageable);
        List<FileListItemDTO> returnList = new ArrayList<>(); //could be LinkedList as well as it won't be modified
        for (FileMetadata fileMetadata : list) {
            FileListItemDTO fileListItemDTO = new FileListItemDTO(fileMetadata);
            if (encodingUtility.isBase32Decodable(fileMetadata.getName())) {
                fileListItemDTO.setName(encodingUtility.decodedBase32SplitArray(fileMetadata.getName())[1]);
            }
            returnList.add(fileListItemDTO);
        }
        return returnList;
    }

    @Transactional
    public FolderMetadata getFolderMetadataFromEncoding(String encodedFolderName, long userId) {
        return queryFolderMetadata(encodingUtility.getMetadataIDFromEncodedBase64(encodedFolderName), userId);
    }

    @Transactional
    public List<FolderListItemDTO> getAllFoldersBelongingToUserAsDTO(long userId) {
        List<FolderMetadata> list = sqLiteFolderRepository.findAllByUserid(userId);
        List<FolderListItemDTO> returnList = new ArrayList<>(); //could be LinkedList as well as it won't be modified
        for (FolderMetadata folderMetadata : list) {
            FolderListItemDTO folderListItemDTO = new FolderListItemDTO(folderMetadata);
            if (encodingUtility.isBase32Decodable(folderMetadata.getName())) {
                folderListItemDTO.setName(encodingUtility.decodedBase32SplitArray(folderMetadata.getName())[1]);
            }
            returnList.add(folderListItemDTO);
        }
        return returnList;
    }

    @Transactional
    public List<FolderListItemDTO> getAllFoldersBelongingToUserAsDTOPageable(long userId, Pageable pageable) {
        Page<FolderMetadata> list = sqLiteFolderRepository.findAllByUseridAndLastUpdatedNotNullOrderByLastUpdatedDesc(userId, pageable);
        List<FolderListItemDTO> returnList = new ArrayList<>(); //could be LinkedList as well as it won't be modified
        for (FolderMetadata folderMetadata : list) {
            FolderListItemDTO folderListItemDTO = new FolderListItemDTO(folderMetadata);
            if (encodingUtility.isBase32Decodable(folderMetadata.getName())) {
                folderListItemDTO.setName(encodingUtility.decodedBase32SplitArray(folderMetadata.getName())[1]);
            }
            returnList.add(folderListItemDTO);
        }
        return returnList;
    }

    public ThumbnailMetadata queryThumbnailMetadataUsingFileId(long fileId, long userId) {
        Optional<ThumbnailMetadata> thumbnailMetadata = sqLiteThumbnailRepository
                .findByFileId(fileId)
                .filter(tm -> tm.getUserId() == userId);
        return thumbnailMetadata.orElse(null);
    }

    @Transactional
    public FolderMetadata queryFolderMetadata(long folderId, long userId) {
        return sqLiteFolderRepository.findById(folderId).filter(f -> f.getUserid() == userId).orElse(null);
    }

    @Transactional
    public List<FolderMetadata> getChildrenFoldersInDirectory(String idPath) {
        return sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath);
    }

    @Transactional
    public List<FolderMetadata> findAllByIdInSQLFolderMetadata(List<Long> folderIdList, long userId) {
        return sqLiteFolderRepository.findAllById(folderIdList).stream()
                .filter(f -> f.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ThumbnailMetadata> findAllThumbnailsByUserID(long userId) {
        return sqLiteThumbnailRepository.findAllByUserId(userId);
    }

    @Transactional
    public List<FileMetadata> findAllByIdInSQLFileMetadata(List<Long> fileIdList, long userId) {
        return sqLiteFileRepository.findAllById(fileIdList).stream()
                .filter(fl -> fl.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileMetadata getFileMetadataByFolderIdNameAndUserId(long folderId, String name, long userId) {
        // dummy metadata for search
        FileMetadata dummyFileMetadata = new FileMetadata();
        dummyFileMetadata.setName(name);
        dummyFileMetadata.setFolderId(folderId);
        dummyFileMetadata.setUserid(userId);
        dummyFileMetadata.setMimiType(null);
        dummyFileMetadata.setSize(null);
        dummyFileMetadata.setId(null);
        dummyFileMetadata.setCreatedAt(null);
        dummyFileMetadata.setHasThumbnail(false);
        Example<FileMetadata> fileMetadataWithoutThumbnailExample = Example.of(dummyFileMetadata);
        Optional<FileMetadata> optionalFileMetadataWithoutThumbnail = sqLiteFileRepository.findOne(fileMetadataWithoutThumbnailExample);
        logger.info("Example without thumbnail {}", dummyFileMetadata);
        if (optionalFileMetadataWithoutThumbnail.isPresent()) {
            return optionalFileMetadataWithoutThumbnail.get();
        }
        dummyFileMetadata.setHasThumbnail(true);
        Example<FileMetadata> fileMetadataWithThumbnailExample = Example.of(dummyFileMetadata);
        Optional<FileMetadata> optionalFileMetadataWithThumbnail = sqLiteFileRepository.findOne(fileMetadataWithThumbnailExample);
        logger.info("Example with thumbnail {}", dummyFileMetadata);
        return optionalFileMetadataWithThumbnail.orElse(null);
    }

    @Transactional
    public FolderMetadata getFolderMetadataFromIdPathAndName(String idPath, String name, long userId) {
        // dummy metadata for search
        FolderMetadata dummyFolderMetadata = new FolderMetadata();
        dummyFolderMetadata.setName(name);
        dummyFolderMetadata.setPath(idPath);
        dummyFolderMetadata.setId(null);
        dummyFolderMetadata.setCreatedAt(null);
        dummyFolderMetadata.setUserid(userId); //current logged-in user id
        logger.info("Example folder: {}", dummyFolderMetadata);
        return sqLiteFolderRepository.findOne(Example.of(dummyFolderMetadata)).orElse(null);
    }

    @Transactional
    public List<FolderMetadata> findAllStartsWithIdPath(String prefixIdPath, long userId) {
        return sqLiteFolderRepository.findAll()
                .stream().filter(fl -> {
                    //handle null values if they exist
                    if (fl.getPath() == null || fl.getUserid() == null) return false;
                    return fl.getPath().startsWith(prefixIdPath) && fl.getUserid() == userId;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Long> findAllStartsWithIdPathReturnsLongList(String prefixIdPath, long userId) {
        return sqLiteFolderRepository.findAll()
                .stream().filter(fl -> {
                    //handle null values if they exist
                    if (fl.getPath() == null || fl.getUserid() == null) return false;
                    return fl.getPath().startsWith(prefixIdPath) && fl.getUserid() == userId;
                }).map(FolderMetadata::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<FileMetadata> findAllFilesWithoutThumbnails(long userId) {
        return sqLiteFileRepository.findAllByUseridAndHasThumbnail(userId, false)
                .stream()
                .filter(fl -> thumbnailProperties.isAllowedImageFormat(fl.getMimiType()))
                .toList();
    }

    @Transactional
    public List<FileMetadata> findAllFilesWithoutThumbnailsInFolder(long folderId, long userId) {
        return sqLiteFileRepository.findAllByUseridAndHasThumbnailAndFolderId(userId, false, folderId)
                .stream()
                .filter(fl -> thumbnailProperties.isAllowedImageFormat(fl.getMimiType()))
                .toList();
    }

    /**
     * Returns ID path of folder with folderId
     *
     * @param folderId folderId of folder
     * @return if folderId is not 0 returns folder's ID path else "0"
     */
    @Transactional
    public String getIdPath(long folderId, long userId) {
        FolderMetadata folderMetadata = queryFolderMetadata(folderId, userId);
        return folderMetadata != null ? folderMetadata.getPath() : "0";
    }

    @Transactional
    public void deleteAllUserRelatedEntries(long userId) {
        sqLiteFileRepository.deleteAllByUserid(userId);
        sqLiteFolderRepository.deleteAllByUserid(userId);
    }
}
