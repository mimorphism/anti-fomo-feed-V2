package com.mimorphism.antifomofeedV2.exceptions;

import com.mimorphism.antifomofeedV2.repository.Channel;

public class NothingToExportException extends RuntimeException {
    public NothingToExportException(Channel channel) {
        super(String.format("Export failed for channel: %s of server: %s", channel.getName(), channel.getServerName()));
    }
}
