mkdir -p build
find src -name "*.java" | xargs javac -d build
find src -type f -not -name "*.java" -exec cp --parents {} build \;
cp -rf build/src/* build
rm -r build/src
cp README.md License.txt build
cd build
jar cfm ../rars.jar ./META-INF/MANIFEST.MF *
