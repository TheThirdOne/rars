#!/bin/bash
mkdir -p build
find ./src/main/java/rars -name "*.java" | xargs javac -d build 
cp -r ./src/main/resources/* build
cd build
jar cfm ../rars.jar ../src/main/resources/META-INF/MANIFEST.MF *
