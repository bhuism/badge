#!/bin/sh

#sudo rm -Rf target src/main/resources/META-INF/native-image
sudo rm -Rf target

IMAGE=oracle/graalvm-ce:20.1.0-java11
  
mkdir -p src/main/resources/META-INF/native-image

docker run --rm \
    --volume $(pwd):/build \
    --workdir /build \
    --volume "$HOME"/.m2:/root/.m2 \
    -e NATIVEPROFILE=true \
    -e COMMIT_SHA=`git rev-parse HEAD` \
    -e SHORT_SHA=`git rev-parse --short HEAD` \
    -e BRANCH_NAME=`git rev-parse --abbrev-ref HEAD` \
    $IMAGE \
    ./build.sh

docker rmi -f badge:latest
docker build -t badge:latest .
docker run -it -e GITHUB_TOKEN -e GITLAB_URL -e GITLAB_TOKEN -p 8080:8080 badge:latest
