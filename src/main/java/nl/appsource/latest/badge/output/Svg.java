package nl.appsource.latest.badge.output;

import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.lib.Widths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonSerializableSchema
public class Svg implements Output<String> {

    private String template;

    @Value("classpath:/template.svg")
    private Resource templateSvg;

    @PostConstruct
    private void postConstruct() throws IOException {
        template = FileCopyUtils.copyToString(new InputStreamReader(templateSvg.getInputStream(), UTF_8));
    }

    public String create(final BadgeStatus badgeStatus, final String message, final String label) {
        return createImage(StringUtils.hasText(label) ? label : badgeStatus.getLabelText(), message, badgeStatus.getLabelColor(), badgeStatus.getMessageColor());
    }

    private String createImage(final String labelText, final String messageText, final String labelColor, final String messageColor) {

        final Properties properties = new Properties();

        double leftTextWidth = Widths.getWidthOfString(labelText) / 10.0;
        double rightTextWidth = Widths.getWidthOfString(messageText) / 10.0;

        double leftWidth = leftTextWidth + 10 + 14 + 3;
        double rightWidth = rightTextWidth + 10;

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
