package nl.appsource.latest.badge.output;

import nl.appsource.latest.badge.controller.BadgeStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.appsource.latest.badge.lib.Widths.getWidthOfString;

public class Svg implements Output<String> {

    private final String template;

    public Svg() throws IOException {
        template = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("/template.svg").getInputStream(), UTF_8));
    }

    public String create(final BadgeStatus badgeStatus) {
        return createSvgImage(badgeStatus.getStatus().getLabelText(), badgeStatus.getMessageText(), badgeStatus.getStatus().getLabelColor(), badgeStatus.getStatus().getMessageColor());
    }

    private String createSvgImage(final String labelText, final String messageText, final String labelColor, final String messageColor) {

        final Properties properties = new Properties();

        final double leftTextWidth = getWidthOfString(labelText) / 10.0;
        final double rightTextWidth = getWidthOfString(messageText) / 10.0;
        final double leftWidth = leftTextWidth + 10 + 14 + 3;
        final double rightWidth = rightTextWidth + 10;
        final double totalWidth = leftWidth + rightWidth;
        final int logoWidth = 14;
        final int logoPadding = 3;

        properties.put("labelText", labelText);
        properties.put("messageText", messageText);
        properties.put("totalWidth", "" + totalWidth);
        properties.put("labelWidth", "" + leftWidth);
        properties.put("messageWidth", "" + rightWidth);
        properties.put("labelBackgroudColor", labelColor);
        properties.put("messageBackgroudColor", messageColor);
        properties.put("logoWidth", "" + logoWidth);
        properties.put("labelTextX", "" + (((leftWidth + logoWidth + logoPadding) / 2.0) + 1) * 10);
        properties.put("labelTextLength", "" + (leftWidth - (10 + logoWidth + logoPadding)) * 10);
        properties.put("messageTextX", "" + ((leftWidth + rightWidth / 2.0) - 1) * 10);
        properties.put("messageTextLength", "" + (rightWidth - 10) * 10);

        return new PropertyPlaceholderHelper("${", "}", null, false).replacePlaceholders(template, properties);

    }

}
