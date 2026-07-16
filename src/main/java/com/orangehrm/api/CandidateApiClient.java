package com.orangehrm.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import static io.restassured.RestAssured.given;

/**
 * REST Assured client for the recruitment candidate API - covers the bonus
 * task in the assessment (add a candidate via API, delete a candidate via API).
 *
 * OrangeHRM doesn't publish a documented public API for recruitment, so we
 * authenticate the same way the SPA does: pick up the session cookies from
 * the Selenium-driven browser after UI login and reuse them for the API calls.
 */
public class CandidateApiClient {

    private final String baseUrl;

    public CandidateApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private RequestSpecification authenticatedRequest(WebDriver driver) {
        RequestSpecification request = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        for (Cookie cookie : driver.manage().getCookies()) {
            request.cookie(cookie.getName(), cookie.getValue());
        }
        return request;
    }

    public Response addCandidate(WebDriver driver, String firstName, String lastName, String email) {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}",
                firstName, lastName, email);

        return authenticatedRequest(driver)
                .body(body)
                .when()
                .post("/web/index.php/api/v2/recruitment/candidates");
    }

    public Response getCandidates(WebDriver driver) {
        return authenticatedRequest(driver)
                .when()
                .get("/web/index.php/api/v2/recruitment/candidates");
    }

    public Response deleteCandidates(WebDriver driver, java.util.List<Integer> candidateIds) {
        return authenticatedRequest(driver)
                .body(java.util.Map.of("ids", candidateIds))
                .when()
                .delete("/web/index.php/api/v2/recruitment/candidates");
    }
}
