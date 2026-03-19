package com.sparta.spartatigers.domain.matchAttendance.model;

import java.util.ArrayList;
import java.util.List;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.user.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "match_attendance")
public class MatchAttendance extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_id")
	private Match match;

	private String contents;

	@OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AttendanceImage> images = new ArrayList<>();

	private String seat;

	private MatchAttendance(User user, Match match, String contents, String seat) {
		this.user = user;
		this.match = match;
		this.contents = contents;
		this.seat = seat;
	}

	public static MatchAttendance create(User user, Match match, String contents, String seat) {
		return new MatchAttendance(user, match, contents, seat);
	}

	public void addImage(String imageUrl, AttendanceImageType imageType) {
		AttendanceImage image = AttendanceImage.create(this,imageUrl, imageType);
		this.images.add(image);
	}

	public void update(String contents, String seat) {
		this.contents = contents;
		this.seat = seat;
	}

	public void clearImages() {
		this.images.clear();
	}

}
