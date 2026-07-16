package com.orangehrm.tests;

import com.orangehrm.api.CandidateApiClient;
import com.orangehrm.config.ConfigReader;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Bonus task from the assessment: add a candidate via API, then delete a
 * candidate via API. Uses the browser session (from BaseTest login) to
 * authenticate the REST Assured calls.
 */
public class CandidateApiTest extends BaseTest {

    @Test(description = "Add a candidate via the recruitment API")
    public void addCandidate_viaApi() {
        loginAsAdmin(); // establishes the session in the browser

        CandidateApiClient apiClient = new CandidateApiClient(ConfigReader.getBaseUrl());
        String uniqueEmail = "qa.auto." + System.currentTimeMillis() + "@example.com";

        Response response = apiClient.addCandidate(driver, "QA", "Automation", uniqueEmail);

        Assert.assertEquals(response.statusCode(), 200,
                "Add candidate should succeed. Body: " + response.getBody().asString());
    }

    @Test(description = "Delete a candidate via the recruitment API",
            dependsOnMethods = "addCandidate_viaApi")
    public void deleteCandidate_viaApi() {
        loginAsAdmin();

        CandidateApiClient apiClient = new CandidateApiClient(ConfigReader.getBaseUrl());
        Response listResponse = apiClient.getCandidates(driver);
        int firstCandidateId = listResponse.jsonPath().getInt("data[0].id");

        Response deleteResponse = apiClient.deleteCandidates(driver, List.of(firstCandidateId));

        Assert.assertEquals(deleteResponse.statusCode(), 200,
                "Delete candidate should succeed. Body: " + deleteResponse.getBody().asString());
    }
}
