package com.mimorphism.antifomofeedV2.exceptions;

import com.mimorphism.antifomofeedV2.repository.Channel;

public class DCEChannelDoesntExistException extends RuntimeException {
    public DCEChannelDoesntExistException(Channel channel) {
        super(String.format("Channel %s of server %s does not exist", channel.getName(), channel.getServerName()));
    }
}
