package com.mimorphism.antifomofeedV2.exceptions;

import com.mimorphism.antifomofeedV2.repository.Channel;

public class DCEAccessDeniedException extends RuntimeException {
    public DCEAccessDeniedException(Channel channel) {
        super(String.format("Access denied for channel: %s of server: %s", channel.getName(), channel.getServerName()));
    }
}
