package com.cloud.NetworkCloudDrive.DAO;

import com.cloud.NetworkCloudDrive.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Repositories.SQL.SQLiteFileRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQL.SQLiteFolderRepository;
import com.cloud.NetworkCloudDrive.Repositories.SQL.SQLiteUserEntityRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Basically DAO but for multiple types*
@Component
public class SQLiteDAO {
    private final Logger logger = LoggerFactory.getLogger(SQLiteDAO.class);
    private final EntityManager entityManager;
    private final SQLiteFolderRepository sqLiteFolderRepository;
    private final SQLiteFileRepository sqLiteFileRepository;
    private final SQLiteUserEntityRepository sqLiteUserEntityRepository;

    public SQLiteDAO(
            SQLiteFolderRepository sqLiteFolderRepository,
            SQLiteFileRepository sqLiteFileRepository,
            SQLiteUserEntityRepository sqLiteUserEntityRepository,
            EntityManager entityManager) {
        this.sqLiteFolderRepository = sqLiteFolderRepository;
        this.sqLiteFileRepository = sqLiteFileRepository;
        this.sqLiteUserEntityRepository = sqLiteUserEntityRepository;
        this.entityManager = entityManager;
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
    public List<FolderMetadata> saveAllFolders(List<FolderMetadata> folderMetadata) {
        return sqLiteFolderRepository.saveAll(folderMetadata);
    }

    @Transactional
    public List<FileMetadata> saveAllFiles(List<FileMetadata> fileMetadata) {
        return sqLiteFileRepository.saveAll(fileMetadata);
    }

    // Database service layer

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
    public UserEntity findUserByNameAndMail(String name, String mail) throws SQLException {
        Optional<UserEntity> userOptional = sqLiteUserEntityRepository.findOne(Example.of(setupExampleUser(name, mail)));
        if (userOptional.isEmpty()) throw new SQLException("User does not exist");
        return userOptional.get();
    }

    @Transactional
    public UserEntity findUserByMail(String mail) throws UsernameNotFoundException {
        Optional<UserEntity> userOptional = sqLiteUserEntityRepository.findByMail(mail);
        if (userOptional.isEmpty())
            throw new UsernameNotFoundException("User not found by mail " + mail);
        return userOptional.get();
    }

    @Transactional
    public List<FolderMetadata> findAllContainingSectionOfIdPathIgnoreCase(String idPath, long userId) {
        return sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath).stream()
                .filter(f -> f.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileMetadata queryFileMetadata(long fileId, long userId) throws SQLException {
        Optional<FileMetadata> fileMetadata = sqLiteFileRepository.findById(fileId).filter(fl -> fl.getUserid() == userId);
        if (fileMetadata.isEmpty())
            throw new SQLException("File with Id " + fileId + " does not exist");
        return fileMetadata.get();
    }

    @Transactional
    public FolderMetadata queryFolderMetadata(long folderId, long userId) throws SQLException {
        Optional<FolderMetadata> folderMetadata = sqLiteFolderRepository.findById(folderId).filter(f -> f.getUserid() == userId);
        if (folderMetadata.isEmpty())
            throw new SQLException("Folder with Id " + folderId + " does not exist");
        return folderMetadata.get();
    }

    @Transactional
    public List<FolderMetadata> getChildrenFoldersInDirectory(String idPath) throws SQLException {
        List<FolderMetadata> findAllByPathList = sqLiteFolderRepository.findAllByPathContainsIgnoreCase(idPath);
        if (findAllByPathList.isEmpty())
            throw new SQLException("Can't find folders at idPath " + idPath + " in database");
        return findAllByPathList;
    }

    @Transactional
    public List<FolderMetadata> findAllByIdInSQLFolderMetadata(List<Long> folderIdList, long userId) {
        return sqLiteFolderRepository.findAllById(folderIdList).stream()
                .filter(f -> f.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<FileMetadata> findAllByIdInSQLFileMetadata(List<Long> fileIdList, long userId) {
        return sqLiteFileRepository.findAllById(fileIdList).stream()
                .filter(fl -> fl.getUserid() == userId)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileMetadata getFileMetadataByFolderIdNameAndUserId(long folderId, String name, long userId) throws FileSystemException {
        // dummy metadata for search
        FileMetadata dummyFileMetadata = new FileMetadata();
        dummyFileMetadata.setName(name);
        dummyFileMetadata.setFolderId(folderId);
        dummyFileMetadata.setUserid(userId);
        dummyFileMetadata.setMimiType(null);
        dummyFileMetadata.setSize(null);
        dummyFileMetadata.setId(null);
        dummyFileMetadata.setCreatedAt(null);
        Example<FileMetadata> fileMetadataExample = Example.of(dummyFileMetadata);
        Optional<FileMetadata> optionalFileMetadata = sqLiteFileRepository.findOne(fileMetadataExample);
        if (optionalFileMetadata.isEmpty())
            throw new FileSystemException("File not found in database. Is database synced?");
        return optionalFileMetadata.get();
    }

    @Transactional
    public FolderMetadata getFolderMetadataFromIdPathAndName(String idPath, String name, long userId) throws FileSystemException {
        // dummy metadata for search
        FolderMetadata dummyFolderMetadata = new FolderMetadata();
        dummyFolderMetadata.setName(name);
        dummyFolderMetadata.setPath(idPath);
        dummyFolderMetadata.setId(null);
        dummyFolderMetadata.setCreatedAt(null);
        dummyFolderMetadata.setUserid(userId); //current logged in user id
        Example<FolderMetadata> folderMetadataExample = Example.of(dummyFolderMetadata);
        Optional<FolderMetadata> optionalFolderMetadata = sqLiteFolderRepository.findOne(folderMetadataExample);
        if (optionalFolderMetadata.isEmpty())
            throw new FileSystemException("Folder not found in database. Is database synced?");
        return optionalFolderMetadata.get();
    }

    @Transactional
    public List<FolderMetadata> findAllStartsWithIdPath(String prefixIdPath) {
        return sqLiteFolderRepository.findAll()
                .stream().filter(fl -> fl.getPath().startsWith(prefixIdPath))
                .collect(Collectors.toList());
    }
}
