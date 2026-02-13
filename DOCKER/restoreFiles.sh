#!/bin/bash
if [[ $# = 0 ]]; then
  echo "Provide Docker Container name"
  exit 0;
fi
docker_container_name=$1
echo "Starting Docker Restore script"
echo "Looking for docker container with name ${docker_container_name}"
container_id=$(docker ps -aqf "name=${docker_container_name}")
echo "Found CONTAINER ID: ${container_id}"
echo "Stopping container ${container_id}"
docker stop $container_id
echo "Stopped container"
echo "Looking for source folder"
source=$(ls -d NetworkCloudDriveDocker_* | sort | tail -n 1)
echo "Source folder: ${source}"
docker cp ./$source/root $container_id:app/
echo "Copied ./$source/root folder to ${container_id}:app/"
docker cp ./$source/filedatabase.db $container_id:app/
echo "Copied ./$source/filedatabase.db to ${container_id}:app/"
docker cp ./$source/config $container_id:app/
echo "Copied ./$source/config to ${container_id}:app/"
docker start $container_id
echo "Started container ${container_id}"
echo "Finished restoring files to Docker Container"