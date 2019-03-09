#!/bin/bash
openssl aes-256-cbc -K $encrypted_ee51214d0271_key -iv $encrypted_ee51214d0271_iv -in android.jks.enc -out android.jks -d
rvm install 2.3.1
gem install supply
./gradlew :app:buildRelease
