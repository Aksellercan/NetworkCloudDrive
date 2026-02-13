#!/bin/bash
echo "Starting Docker restore script"
container_id=$(docker ps -aqf "name=NetworkCloudDriveDocker")
echo "Found CONTAINER ID: ${container_id}"
echo "Stopping container ${container_id}"
docker stop $container_id
echo "Stopped container"
docker cp ./root $container_id:app/
echo "Copied ./root folder to ${container_id}:app/"
docker cp ./filedatabase.db $container_id:app/
echo "Copied ./filedatabase.db to ${container_id}:app/"
docker cp ./config $container_id:app/
echo "Copied ./config to ${container_id}:app/"
docker start $container_id
echo "Started container ${container_id}"
echo "Finished restoring files to Docker Container"