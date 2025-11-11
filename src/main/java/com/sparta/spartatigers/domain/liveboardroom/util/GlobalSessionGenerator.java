package com.sparta.spartatigers.domain.liveboardroom.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalSessionGenerator {
	public static final String SERVER_ID;

	static {
		String serverId;
		try {
			serverId = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			serverId = "Unknown";
			log.warn("Failed to resolve hostname. Fallback to 'unknown'", e);
		}
		SERVER_ID = serverId;
	}

	public static String generate(String sessionId) {
		return SERVER_ID + "-" + sessionId;
	}
}
