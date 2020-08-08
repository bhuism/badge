package nl.appsource.badge.output;

import nl.appsource.badge.BadgeStatus;
import nl.appsource.badge.model.shieldsio.ShieldsIoResponse;

import static nl.appsource.badge.BadgeStatus.Status.ERROR;

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
