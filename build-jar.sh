mkdir -p build
find rars -name "*.java" | xargs javac -d build 
cp -r images help *.properties License.txt PseudoOps.txt README.md build
cd build
jar cfm ../rars.jar ../META-INF/MANIFEST.MF *

