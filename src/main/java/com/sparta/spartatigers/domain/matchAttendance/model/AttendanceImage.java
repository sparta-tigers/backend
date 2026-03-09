package com.sparta.spartatigers.domain.matchAttendance.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class AttendanceImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	@ManyToOne
	@JoinColumn(name = "attendance_id")
	private MatchAttendance attendance;

	private String s3url;

	@Enumerated(EnumType.STRING)
	private AttendanceImageType imageType;


}
