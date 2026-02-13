#!/bin/bash
echo "Starting Docker Backup script"
container_id=$(docker ps -aqf "name=NetworkCloudDriveDocker")
echo "Found CONTAINER ID: ${container_id}"
# ENTER CONTAINER AND RETRIEVE /app FOLDER
docker cp $container_id:app/ .
echo "Copy complete"
echo "Now extracting root folder, db and config folder"
time_stamp=$(date +%Y-%m-%d-%T)
backup_folder="NetworkCloudDriveDocker_$time_stamp"
mkdir ./$backup_folder
echo "Created folder ${backup_folder}"
mv ./app/filedatabase.db ./$backup_folder
echo "Moved ./app/filedatabase.db"
mv ./app/root ./$backup_folder
echo "Moved ./app/root/"
mv ./app/config ./$backup_folder
echo "Moved ./app/config/"
rm -r ./app
echo "Cleaned up"
echo "Finished backup of Docker Container"