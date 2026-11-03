package com.mygym.app.models;

import java.io.Serial;
import java.io.Serializable;

public record BroadcastMessageDto(String senderId, String content, String image) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
