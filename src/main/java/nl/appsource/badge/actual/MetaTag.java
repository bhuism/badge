package nl.appsource.badge.actual;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.BadgeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class MetaTag implements Function<String, String> {

    private static final int TIMEOUT = 5000;

    @SneakyThrows
    @Override
    public String apply(final String htmlUrl) {

        final Document parse = Jsoup.parse(new URL(htmlUrl), TIMEOUT);

        return parse.getElementsByTag("meta")
            .stream()
            .filter(element -> "ui-version".equals(element.attr("name")))
            .findFirst()
            .map(element -> element.attr("content"))
            .orElseThrow(() -> new BadgeException("can not find ui-version meta header"));

    }
}

