#!/bin/bash
if git submodule status | grep \( > /dev/null ; then 
    version=$(git describe --tags --match 'v*' --dirty | cut -c2-)
    echo "Version = $version" > src/Version.properties
    mkdir -p build
    find src -name "*.java" | xargs javac --release 11 -d build
    if [[ "$OSTYPE" == "darwin"* ]]; then
        find src -type f -not -name "*.java" -exec rsync -R {} build \;
    else
        find src -type f -not -name "*.java" -exec cp --parents {} build \;
    fi
    cp -rf build/src/* build
    rm -r build/src
    cp README.md LICENSE build
    cd build
    jar cfm ../rars.jar ./META-INF/MANIFEST.MF *
    chmod +x ../rars.jar
else
    echo "It looks like JSoftFloat is not cloned. Consider running \"git submodule update --init\""
fi
