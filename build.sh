#!/usr/bin/env bash

mkdir -p target/native-image

cd target/native-image

jar -xf ../badge.jar

cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`

native-image --verbose -cp BOOT-INF/classes:$LIBPATH

cd ..
mv ./native-image/badge .
rm -Rf native-image
cd ..
