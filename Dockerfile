FROM scratch
EXPOSE 8080
COPY target/badge /badge
ENTRYPOINT [ "/badge", "-Djava.io.tmpdir=/", "-Dspring.profiles.active=production", "-XX:+PrintGC", "-XX:+VerboseGC", "--expert-options-all", "-Xmn32m", "-Xmx64m" ]
