package com.sparta.spartatigers.domain.support.weather.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// 기상청 api 로부터 받은 응답 원본 데이터를 담을 래퍼 클래스
@JsonIgnoreProperties(ignoreUnknown = true)
public class OriginResponse {

    @JsonProperty("response")
    public Response response;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        public Header header;
        public Body body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {

        public String resultCode;
        public String resultMsg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        public String dataType;
        public Items items;
        public Integer pageNo;
        public Integer numOfRows;
        public Integer totalCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {

        public List<Item> item;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        public String baseDate;
        public String baseTime;
        public String category; // ✅ 카테고리
        public Integer nx;
        public Integer ny;

        // 예보
        public String fcstDate;
        public String fcstTime;
        public String fcstValue; // ✅ 값

        // 실황
        public String obsrValue; // ✅ 값
    }
}
