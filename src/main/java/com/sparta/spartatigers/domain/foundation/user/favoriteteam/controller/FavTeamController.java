package com.sparta.spartatigers.domain.foundation.user.favoriteteam.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.dto.FavTeamRequestDto;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.dto.FavTeamResponseDto;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.service.FavTeamService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FavTeamController {
	private final FavTeamService favTeamService;

	@PostMapping("/fav")
	public ApiResponse<FavTeamResponseDto> addFavTeam(
		@RequestBody FavTeamRequestDto request,
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.created(favTeamService.add(request, userId));
	}

	@GetMapping("/fav")
	public ApiResponse<FavTeamResponseDto> getFavTeam(
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(favTeamService.get(userId));
	}

	@PatchMapping("/fav")
	public ApiResponse<FavTeamResponseDto> updateFavTeam(
		@RequestBody FavTeamRequestDto request,
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(favTeamService.update(request, userId));
	}

	@DeleteMapping("/fav")
	public ApiResponse<?> deleteFavTeam (
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(favTeamService.delete(userId));
	}
}
