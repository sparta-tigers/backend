package com.sparta.spartatigers.domain.liveboard.matchAttendance.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AttendanceImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attendance_id")
	private MatchAttendance attendance;

	private String imageUrl;

	@Enumerated(EnumType.STRING)
	private AttendanceImageType imageType;

	private AttendanceImage(MatchAttendance attendance, String imageUrl, AttendanceImageType imageType) {
		this.attendance = attendance;
		this.imageUrl = imageUrl;
		this.imageType = imageType;
	}

	public static AttendanceImage create(MatchAttendance attendance, String imageUrl, AttendanceImageType imageType) {
		return new AttendanceImage(attendance, imageUrl, imageType);
	}


}
