#!/bin/sh

flatlaf="flatlaf-3.2.jar"

if [ ! -f "$flatlaf" ]; then
	curl https://repo1.maven.org/maven2/com/formdev/flatlaf/3.2/flatlaf-3.2.jar -o "$flatlaf"
fi

rm -r tmp/
mkdir -p tmp/
cd tmp/


jar x < ../rars.jar
jar x < "../$flatlaf"

cat > META-INF/MANIFEST.MF <<EOF
Manifest-Version: 1.0
Implementation-Version: 3.1.1
Multi-Release: true
Main-Class: rars.Launch
EOF

jar cfm ../rars-flatlaf.jar META-INF/MANIFEST.MF *
