#!/bin/bash
if git submodule status | grep \( > /dev/null ; then
    # Create build folder
    mkdir -p build
    # Compile src/*.java files
    find src -name "*.java" | xargs javac -d build
    # Exit if compilation error happened
    if [ $? -ne 0 ]; then
        exit 1
    fi
    # Copy src/*.java files to build/
    if [[ "$OSTYPE" == "darwin"* ]]; then
        find src -type f -not -name "*.java" -exec rsync -R {} build \;
    else
        find src -type f -not -name "*.java" -exec cp --parents {} build \;
    fi
    cp -rf build/src/* build
    rm -r build/src
    cp README.md License.txt build
    cd build
    # Create .jar file from build/
    jar cfm ../rars.jar ./META-INF/MANIFEST.MF *
else
    echo "It looks like JSoftFloat is not cloned. Consider running \"git submodule update --init\""
fi