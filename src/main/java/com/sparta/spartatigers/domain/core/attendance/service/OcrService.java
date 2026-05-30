package com.sparta.spartatigers.domain.core.attendance.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

	@Value("${google.cloud.vision.credentials.location}")
	private Resource credentialKey;

	public String extractTextFromImage(MultipartFile file) throws IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();
		ByteString imgBytes = ByteString.readFrom(file.getInputStream());
		Image img = Image.newBuilder().setContent(imgBytes).build();

		Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		GoogleCredentials credentials = GoogleCredentials.fromStream(credentialKey.getInputStream());
		ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			StringBuilder resultText = new StringBuilder();
			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					throw new RuntimeException("OCR 인식 오류 : " + res.getError().getMessage());
				}
				resultText.append(res.getFullTextAnnotation().getText());
			}
			return resultText.toString();
		}
	}

	public String parseSeatInfo(String ocrText) {
		// 1. 내/외야 구역
		Pattern zonePattern = Pattern.compile("(1루|3루|외야|중앙)");

		// 2. 좌석 등급/종류
		Pattern typePattern = Pattern.compile("([가-힣a-zA-Z]+(석|존)(\\s*\\([0-9a-zA-Z가-힣]+\\))?)");

		// 3. 구역/블록/블럭
		Pattern blockPattern = Pattern.compile("([0-9a-zA-Z가-힣]+)\\s*(구역|블록|블럭)");

		// 4. 열 (일, 연, 엘 오인식 )
		// 뒤에 -번, -석이 오는것 전부!
		Pattern rowPattern = Pattern.compile("([0-9a-zA-Z]+)\\s*(열|연|엘|일(?=\\s*[0-9]+\\s*(번|석|th|st|nd|rd)))");

		// 5. 번호
		Pattern seatPattern = Pattern.compile("([0-9]+)\\s*(번|석|th|st|nd|rd)");

		Matcher zoneMatcher = zonePattern.matcher(ocrText);
		Matcher typeMatcher = typePattern.matcher(ocrText);
		Matcher blockMatcher = blockPattern.matcher(ocrText);
		Matcher rowMatcher = rowPattern.matcher(ocrText);
		Matcher seatMatcher = seatPattern.matcher(ocrText);

		// --- 매칭 로직 ---
		String zone = zoneMatcher.find() ? zoneMatcher.group(1) : "";

		// '좌석'으로 시작하면 무시하고 다음 찾기
		String type = "";
		while (typeMatcher.find()) {
			String foundType = typeMatcher.group(1);
			if (!foundType.startsWith("좌석")) {
				type = foundType;
				// (1)이나 (3)으로 짤린 글자에 '루'를 얹기
				type = type.replace("(3)", "(3루)").replace("(1)", "(1루)");
				break;
			}
		}

		String block = blockMatcher.find() ? blockMatcher.group(1) + blockMatcher.group(2) : "";
		String row = rowMatcher.find() ? rowMatcher.group(1) + "열" : "";
		String seat = seatMatcher.find() ? seatMatcher.group(1) + "번" : "";

		// 파싱된 결과를 순서대로 조합
		String parsedSeat = String.format("%s %s %s %s %s", zone, type, block, row, seat);
		parsedSeat = parsedSeat.replaceAll("\\s+", " ").trim();

		return parsedSeat.isEmpty() ? "좌석 정보를 인식하지 못했습니다." : parsedSeat;

	}
}
