package com.cloud.NetworkCloudDrive.Repositories.SQL;

import com.cloud.NetworkCloudDrive.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SQLiteUserEntityRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByName(String name);

    Optional<UserEntity> findByMail(String mail);
}
