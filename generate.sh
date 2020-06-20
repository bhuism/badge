#!/bin/sh

sudo rm -Rf target

docker run -it --rm \
    --volume $(pwd):/build \
    --workdir /build \
    --volume "$HOME"/.m2:/root/.m2 \
    -p 8080:8080 \
    oracle/graalvm-ce:20.1.0-java11 \
    bash -c './build.sh ; $JAVA_HOME/bin/java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/badge.jar'


