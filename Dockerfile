# Etapa 1: Build
FROM gradle:8.13-jdk21 AS build
WORKDIR /app
# Copia todos os arquivos do projeto para o diretório de trabalho do contêiner
COPY . .
# Executa o build do Gradle para gerar o arquivo .jar
RUN gradle clean build -x test

# Etapa 2: Runtime
FROM amazoncorretto:21
WORKDIR /app
# Copia para um diretório e renomeia em seguida
COPY --from=build /app/build/libs/*.jar ./
RUN mv $(ls *.jar | grep -v plain) app.jar
# Expõe a porta que a aplicação irá usar (por exemplo, 8080)
EXPOSE 8080
# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]