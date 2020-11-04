#!/bin/sh

rm -Rf target
mkdir target

gu install --no-progress native-image

MAVEN_VERSION=3.6.3
curl -sL /tmp/maven.tar.gz https://www-us.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xz -C /opt
ln -s /opt/apache-maven-$MAVEN_VERSION /opt/maven

export PATH=$PATH:/opt/maven/bin

mkdir /build

export PATH=$PATH:/build/bundle/bin

echo Starting maven
mvn -U -B -DskipTests package

