#!/bin/bash
if [[ $# = 0 ]]; then
  echo "Provide Docker Container name"
  exit 0;
fi
echo "Starting Docker Backup script"
docker_container_name=$1
echo "Looking for docker container with name ${docker_container_name}"
container_id=$(docker ps -aqf "name=${docker_container_name}")
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