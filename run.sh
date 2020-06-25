#!/bin/sh

#sudo rm -Rf target src/main/resources/META-INF/native-image
  
mkdir -p src/main/resources/META-INF/native-image

export COMMIT_SHA=`git rev-parse HEAD`
export SHORT_SHA=`git rev-parse --short HEAD`
export BRANCH_NAME=`git rev-parse --abbrev-ref HEAD`

docker run --rm \
    --volume $(pwd):/build \
    --workdir /build \
    --volume "$HOME"/.m2:/root/.m2 \
    -e NATIVEPROFILE=true \
    -e COMMIT_SHA \
    -e SHORT_SHA \
    -e BRANCH_NAME \
    oracle/graalvm-ce:20.1.0-java11 \
    bash -c './build.sh'

docker build -t badge:latest .

docker run -it -e GITHUB_TOKEN -e GITLAB_URL -e GITLAB_TOKEN -p 8080:8080 badge:latest
