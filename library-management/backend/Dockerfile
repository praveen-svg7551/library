FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir out && javac -d out $(find src -name "*.java")

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/out ./out
ENV DATA_FILE=/tmp/library-data.txt
CMD ["java", "-cp", "out", "library.Main"]
