package nl.appsource.latest.badge.output;

import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.stereotype.Service;

import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;

@Service
public class ShieldsIo implements Output<ShieldsIoResponse> {

    public ShieldsIoResponse create(final BadgeStatus status) {

        final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();

        shieldsIoResponse.setMessage(status.getMessageText());
        shieldsIoResponse.setLabel(status.getStatus().getLabelText());
        shieldsIoResponse.setLabelColor(status.getStatus().getLabelColor());
        shieldsIoResponse.setColor(status.getStatus().getMessageColor());
        shieldsIoResponse.setIsError(ERROR.equals(status.getStatus()));

        //shieldsIoResponse.setMessage(gitHubResponseEntity.getStatusCode().getReasonPhrase());

        return shieldsIoResponse;

    }

}
