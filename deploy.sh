#!/bin/bash
openssl aes-256-cbc -K $encrypted_ee51214d0271_key -iv $encrypted_ee51214d0271_iv -in secrets.tar.enc -out secrets.tar -d
tar xf secrets.tar
echo "$TRAVIS_COMMIT_MESSAGE" > app/src/main/play/release-notes/en-US/default.txt
sed "s/---MY-PASSWORD-HERE---/$KEY_PASSWORD/" -i app/build.gradle
chmod +x gradlew
./gradlew :app:publish
