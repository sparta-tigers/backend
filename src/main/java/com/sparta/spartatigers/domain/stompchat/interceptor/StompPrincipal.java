package com.sparta.spartatigers.domain.stompchat.interceptor;

import java.security.Principal;

public class StompPrincipal implements Principal {

	private final String name; // userid임

	public StompPrincipal(String userId, String nickname) {
		this.name = String.valueOf(userId);
	}

	@Override
	public String getName() {
		return name;
	}

}
