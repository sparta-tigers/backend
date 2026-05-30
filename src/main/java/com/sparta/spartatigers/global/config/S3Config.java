package com.sparta.spartatigers.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class S3Config {

	// S3 사용 대비 설정 클래스
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public AmazonS3 amazonS3() {
		// 1. 야믈에서 읽어온 키로 자격 증명 객체 생성
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		// 2. AmazonS3 클라이언트 빌드 후 스프링 Bean으로 등록
		return AmazonS3ClientBuilder.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.build();
	}
}
