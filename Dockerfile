# Comando para usar a imagem base do Eclipse Temurin com JDK 21
FROM eclipse-temurin:21-jdk

# Diretório da aplicação dentro do container
WORKDIR /app

# Comando que copia o jar gerado pelo maven
COPY target/estoque-backend-fat.jar app.jar

CMD ["java", "-jar", "/app.jar"]

# Comando que baixa o driver JDBC do PostgreSQL
RUN mkdir -p /app/lib \
    && curl -L -o /app/lib/postgresql-42.7.1.jar \
       https://jdbc.postgresql.org/download/postgresql-42.7.1.jar

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-cp", "app.jar:lib/postgresql-42.7.1.jar", "-jar", "app.jar"]
