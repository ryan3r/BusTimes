FROM thyrlian/android-sdk

FROM openjdk:8-jdk
ENV ANDROID_HOME="/usr/local/android-sdk"
COPY --from=0 /opt/android-sdk $ANDROID_HOME
COPY . /mnt
WORKDIR /mnt
RUN chmod +x gradlew && ./gradlew dependencies
CMD ./gradlew test
