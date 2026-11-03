package com.mygym.app.util;

public final class MessageConsumerConstants {

	public static final String NOTIFICATIONS_ENDPOINT = "/ws-notifications";

	public static final String TOPIC_ENDPOINT = "/topic";

	public static final String CONSUMER_REST_CONTROLLER = "/api/consumer/notifications";

	public static final String CONSUMER_REST_CONTROLLER_SAFETY = "/api/consumer/notifications/";

	public static final String SYNC_ENDPOINT = "/sync-history";

	public static final String BROADCAST_TOPIC_CONSTANT = "broadcast-messages";

	public static final String BROADCAST_GROUP_ID_CONSTANT = "mga-notification-group";

	public static final String LIVE_TOPIC_ENDPOINT = "/topic/live-notices";

	public static final String BROADCAST_MESSAGE_TYPE = "BROADCAST";

	public static final String FEED_KEY = "mga:notifications:feed";

	public static final String LIVE_CHANNEL = "mga:notifications:live";

	private MessageConsumerConstants() {
	}
}
