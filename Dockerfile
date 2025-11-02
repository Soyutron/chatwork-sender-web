# =========================
#  Stage 1: Build with Gradle
# =========================
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app
COPY . .

# キャッシュを利用して依存を事前に取得
RUN gradle clean build -x test --no-daemon

# =========================
#  Stage 2: Run minimal JRE
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# アプリが使用するポートを開放
EXPOSE 9090

# Spring Bootの起動コマンド
ENTRYPOINT ["java", "-jar", "app.jar"]
