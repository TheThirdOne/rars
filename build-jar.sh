#!/bin/bash
if git submodule status | grep \( > /dev/null ; then 
    mkdir -p build
    find src -name "*.java" | xargs javac -d build
    if [[ "$OSTYPE" == "darwin"* ]]; then
        find src -type f -not -name "*.java" -exec rsync -R {} build \;
    else
        find src -type f -not -name "*.java" -exec cp --parents {} build \;
    fi
    cp -rf build/src/* build
    rm -r build/src
    cp README.md License.txt build
    cd build
    jar cfm ../rars.jar ./META-INF/MANIFEST.MF *
else
    echo "It looks like JSoftFloat is not cloned. Consider running \"git submodule update --init\""
fi
