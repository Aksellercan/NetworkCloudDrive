#!/bin/bash
echo "Starting Docker backup script"
container_id=$(docker ps -aqf "name=NetworkCloudDriveDocker")
echo "Found CONTAINER ID: ${container_id}"
# ENTER CONTAINER AND RETRIEVE /app FOLDER
docker cp $container_id:app/ .
echo "Copy complete"
echo "Now extracting root folder, db and config folder"
time_stamp=$(date +%Y-%m-%d-%T)
mkdir ./$time_stamp
echo "Created folder ${time_stamp}"
mv ./app/filedatabase.db ./$time_stamp
echo "Moved ./app/filedatabase.db"
mv ./app/root ./$time_stamp
echo "Moved ./app/root/"
mv ./app/config ./$time_stamp
echo "Moved ./app/config/"
rm -r ./app
echo "Cleaned up"
echo "Finished backup of Docker Container"