package nextstep.subway.acceptance;

import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.subway.acceptance.LineRequestCollection.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @Test
    @DisplayName("지하철 노선을 생성")
    public void createLine() {
        // when
        ExtractableResponse<Response> response = 이호선_생성();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();

        JsonPath responseBody = response.jsonPath();
        assertAll(
                () -> assertThat(responseBody.getLong("id")).isNotZero(),
                () -> assertThat(responseBody.getString("name")).isEqualTo("2호선"),
                () -> assertThat(responseBody.getString("color")).isEqualTo("bg-green-600"),
                () -> assertThat(responseBody.getList("stations.name")).contains("강남역", "건대입구역")
        );
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @Test
    @DisplayName("전체 지하철 노선 목록 조회")
    public void searchLines() {
        // given
        일호선_생성();
        이호선_생성();

        // when
        ExtractableResponse<Response> response = 지하철_노선_목록_조회();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        JsonPath responseBody = response.jsonPath();
        assertThat(responseBody.getList("name")).containsExactly("1호선", "2호선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @Test
    @DisplayName("단일 지하철 노선 조회")
    public void searchLine() {
        // given
        long lineId = 이호선_생성().jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = 지하철_단일_노선_조회(lineId);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        JsonPath responseBody = response.jsonPath();

        assertAll(
                () -> assertThat(responseBody.getLong("id")).isEqualTo(lineId),
                () -> assertThat(responseBody.getString("name")).isEqualTo("2호선"),
                () -> assertThat(responseBody.getString("color")).isEqualTo("bg-green-600"),
                () -> assertThat(responseBody.getList("stations.name")).contains("강남역", "건대입구역")
        );
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @Test
    @DisplayName("단일 지하철 노선 편집")
    public void editLine() {
        // given
        long lineId = 이호선_생성().jsonPath().getLong("id");

        // when
        int editStatusCode = 지하철_노선_수정(lineId, "다른 2호선", "연두색");

        // then
        JsonPath responseBody = 지하철_단일_노선_조회(lineId).jsonPath();
        assertAll(
                () -> assertThat(editStatusCode).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(responseBody.getString("name")).isEqualTo("다른 2호선"),
                () -> assertThat(responseBody.getString("color")).isEqualTo("연두색")
        );

    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @Test
    @DisplayName("단일 지하철 노선 삭제")
    public void deleteLine() {
        // given
        long lineId = 이호선_생성().jsonPath().getLong("id");

        // when
        int deleteStatusCode = 지하철_단일_노선_삭제(lineId);

        // then
        assertThat(deleteStatusCode).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

}
