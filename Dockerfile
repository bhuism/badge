FROM scratch
COPY target/badge /badge
ENTRYPOINT [ "/badge", "-Djava.io.tmpdir=/", "-Dspring.profiles.active=production" ]
