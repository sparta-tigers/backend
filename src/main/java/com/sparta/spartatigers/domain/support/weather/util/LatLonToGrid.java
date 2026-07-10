package com.sparta.spartatigers.domain.support.weather.util;

/**
 * 기상청 LCC(Lambert Conformal Conic) 격자 좌표 변환 유틸리티
 *
 * Why: 기상청 단기예보 API는 위경도가 아닌 자체 격자 좌표(nx, ny)를 사용한다.
 * DB에 하드코딩된 nx/ny가 실제 구장 위경도와 수 km 이상 어긋나면
 * 기상청 웹(사용자 위치 기반)과 앱(고정 격자 기반)의 데이터가 달라진다.
 * 이 유틸로 위경도 → 격자 좌표를 재계산해 DB 값의 정확성을 검증할 수 있다.
 *
 * 변환 파라미터 출처: 기상청 단기예보 API 활용 가이드 (격자 변환 공식)
 * - 투영 기준점: 위도 38°N, 경도 126°E
 * - 표준 위도 1: 30°N, 표준 위도 2: 60°N
 * - 격자 간격: 5km
 * - 기준점 격자: (43, 136)
 */
public class LatLonToGrid {

    // ── 기상청 LCC 투영 파라미터 ──────────────────────────────────────────────
    private static final double RE = 6371.00877; // 지구 반경 (km)
    private static final double GRID = 5.0; // 격자 간격 (km)
    private static final double SLAT1 = 30.0; // 표준 위도 1 (°)
    private static final double SLAT2 = 60.0; // 표준 위도 2 (°)
    private static final double OLON = 126.0; // 기준점 경도 (°)
    private static final double OLAT = 38.0; // 기준점 위도 (°)
    private static final double XO = 43.0; // 기준점 격자 X
    private static final double YO = 136.0; // 기준점 격자 Y

    private static final double DEGRAD = Math.PI / 180.0;

    // 사전 계산된 투영 상수 (정적 초기화)
    private static final double SN;
    private static final double SF;
    private static final double RO;

    static {
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5)
                / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        SN = sn;

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        SF = sf;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        RO = (RE / GRID) * SF / Math.pow(ro, SN);
    }

    /**
     * 위경도 → 기상청 격자 좌표 변환
     *
     * @param lat 위도 (°, WGS84)
     * @param lon 경도 (°, WGS84)
     * @return int[]{nx, ny}
     */
    public static int[] toGrid(double lat, double lon) {
        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = (RE / GRID) * SF / Math.pow(ra, SN);

        double theta = lon * DEGRAD - OLON * DEGRAD;
        if (theta > Math.PI)
            theta -= 2.0 * Math.PI;
        if (theta < -Math.PI)
            theta += 2.0 * Math.PI;
        theta *= SN;

        int nx = (int) (ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) (RO - ra * Math.cos(theta) + YO + 0.5);
        return new int[] { nx, ny };
    }

    /**
     * DB에 저장된 nx/ny와 위경도로 재계산한 값을 비교해 오차를 반환한다.
     *
     * @param lat  구장 위도
     * @param lon  구장 경도
     * @param dbNx DB에 저장된 nx
     * @param dbNy DB에 저장된 ny
     * @return 격자 단위 최대 오차 (0이면 완전 일치, 1 이상이면 5km 이상 오차)
     */
    public static int maxGridError(double lat, double lon, int dbNx, int dbNy) {
        int[] calc = toGrid(lat, lon);
        return Math.max(Math.abs(calc[0] - dbNx), Math.abs(calc[1] - dbNy));
    }
}
