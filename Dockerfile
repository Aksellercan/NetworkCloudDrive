FROM amazoncorretto:17-alpine

# Set volumes and work directory
VOLUME /tmp
VOLUME /app/root

# Set work directory
WORKDIR /app

# Update repos and install maven (alpine apk)
RUN apk update
RUN apk add maven

# Add pom and resolve dependencies
ADD pom.xml /app/pom.xml
RUN mvn dependency:resolve

# Adding source, verify, compile and package into a fat jar
ADD src /app/src
RUN mvn verify
RUN mvn package

# Rename file from NetworkCloudDrive-0.0.1-SNAPSHOT.jar to NetworkCloudDriveDocker.jar
RUN mv /app/target/*.jar /app/target/NetworkCloudDriveDocker.jar
# Expose port 8080, you want ENTRYPOINT or CMD to be last final step of dockerfile
EXPOSE 8080

# Finally run the API
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container","-jar","/app/target/NetworkCloudDriveDocker.jar"]
