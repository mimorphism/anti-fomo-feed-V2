package com.mimorphism.antifomofeedV2.exceptions;

import com.mimorphism.antifomofeedV2.repository.Channel;

public class DCENothingToExportException extends RuntimeException {
    public DCENothingToExportException(Channel channel) {
        super(String.format("Export failed for channel: %s of server: %s", channel.getName(), channel.getServerName()));
    }
}
