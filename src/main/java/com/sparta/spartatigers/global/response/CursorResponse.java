package com.sparta.spartatigers.global.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorResponse<T, U> {
	List<T> data;
	U nextCursor;
	boolean hasNext;

	public static <T, U> CursorResponse<T, U> of(List<T> data, U nextCursor, boolean hasNext) {
		return new CursorResponse<>(data, nextCursor, hasNext);
	}
}
