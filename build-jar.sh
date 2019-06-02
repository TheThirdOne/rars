#!/bin/bash
mkdir -p build
find ./src/main/java/rars -name "*.java" | xargs javac -d build

# Copy resources to jar
cp -r ./src/main/resources/* build

# Copy README.md to jar
cp README.md screenshot.png build

cd build
jar cfm ../rars.jar ../src/main/resources/META-INF/MANIFEST.MF *
