FROM debian:buster-slim
COPY target/badge /app/badge
RUN chmod +x /app/badge
EXPOSE 8080
ENTRYPOINT [ "/app/badge" ]
