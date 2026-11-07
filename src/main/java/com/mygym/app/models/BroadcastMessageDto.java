package com.mygym.app.models;

import java.io.Serializable;

public record BroadcastMessageDto(String senderId, String content, String image) implements Serializable {
}
