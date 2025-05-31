FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
# Install curl
RUN apk add --no-cache curl
COPY target/*.jar app.jar
COPY wait-for-services.sh /wait-for-services.sh
RUN chmod +x /wait-for-services.sh 
ENTRYPOINT ["/wait-for-services.sh"]