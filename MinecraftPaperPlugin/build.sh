#!/bin/bash

# QuizPlugin Build Script
# This script builds the plugin JAR file using Maven

echo "======================================"
echo "QuizPlugin Build Script"
echo "======================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed!"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    echo ""
    exit 1
fi

# Display Maven version
echo "Maven version:"
mvn -version
echo ""

# Clean and build
echo "Building QuizPlugin..."
echo ""
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "Build Successful!"
    echo "======================================"
    echo ""
    echo "The plugin JAR file is located at:"
    echo "  target/QuizPlugin-1.0.0.jar"
    echo ""
    echo "Copy this file to your server's plugins/ folder"
    echo ""
else
    echo ""
    echo "======================================"
    echo "Build Failed!"
    echo "======================================"
    echo ""
    echo "Please check the error messages above"
    echo ""
    exit 1
fi
