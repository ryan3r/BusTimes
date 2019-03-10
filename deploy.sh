#!/bin/bash
openssl aes-256-cbc -K $encrypted_ee51214d0271_key -iv $encrypted_ee51214d0271_iv -in secrets.tar.enc -out secrets.tar -d
tar xf secrets.tar
sed "s/---MY-PASSWORD-HERE---/$KEY_PASSWORD/g" -i app/build.gradle
cat app/build.gradle
#chmod +x gradlew
#./gradlew :app:publish
