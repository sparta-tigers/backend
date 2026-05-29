#!/bin/bash
set -e

echo "=== Spotless Check ==="
./gradlew spotlessCheck

echo "=== Compile ==="
./gradlew compileJava

echo "=== Test ==="
./gradlew test

echo "✅ All checks passed."
