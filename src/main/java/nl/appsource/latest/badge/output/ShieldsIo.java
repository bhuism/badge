package nl.appsource.latest.badge.output;

import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static nl.appsource.latest.badge.controller.BadgeStatus.ERROR;

@Service
public class ShieldsIo implements Output<ShieldsIoResponse> {

    public ShieldsIoResponse create(final BadgeStatus status, final String message, final String label) {

        final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();

        shieldsIoResponse.setMessage(message);
        shieldsIoResponse.setLabel(StringUtils.hasText(label) ? label : status.getLabelText());
        shieldsIoResponse.setLabelColor(status.getLabelColor());
        shieldsIoResponse.setColor(status.getMessageColor());
        shieldsIoResponse.setIsError(ERROR.equals(status));

        //shieldsIoResponse.setMessage(gitHubResponseEntity.getStatusCode().getReasonPhrase());

        return shieldsIoResponse;

    }

}
