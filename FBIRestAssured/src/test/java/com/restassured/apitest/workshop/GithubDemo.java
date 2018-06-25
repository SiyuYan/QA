package com.restassured.apitest.workshop;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.w3c.dom.events.EventException;

import java.io.FileReader;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GithubDemo {
    private static String repoNameForView = "agileTour";
    private static String repoName = "Hello-World";
    private static String ownerName = "SiyuYan";
    private static String path = "https://api.github.com/";
    private static String authorization = "Basic U2l5dVlhbjoyNzM5MzkzeXN5";

    @Test
    public void testListRepo() throws EventException {
        given().
                headers("Authorization", authorization).
                log().all().
                when().
                get(path + "user/repos").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void testGetRepo() throws EventException {
        given().
                log().all().
                when().
                get(path + "repos/" + ownerName + "/" + repoNameForView).
                then().
                assertThat().
                statusCode(200).
                and().
                body("name", response -> equalTo(repoNameForView)).
                body("owner.login", response -> equalTo(ownerName));
    }

    @Test
    public void testCreateRepo() throws EventException {
        String requestData = "{\n" +
                "  \"name\": \"Hello-World\",\n" +
                "  \"description\": \"This is your first repository\",\n" +
                "  \"homepage\": \"https://github.com\",\n" +
                "  \"private\": false,\n" +
                "  \"has_issues\": true,\n" +
                "  \"has_projects\": true,\n" +
                "  \"has_wiki\": true\n" +
                "}";
        given().
                headers("Authorization", authorization).
                log().all().
                body(requestData).
                when().
                post(path + "user/repos").
                then().
                assertThat().
                statusCode(201). //201 Created
                and().
                body("name", response -> equalTo(repoName)).
                body("description", response -> equalTo("This is your first repository"));
    }

    @Test
    public void testEditRepo() throws EventException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("src/test/resources/repo_data.json")); //the location of the file
        JSONObject editData = (JSONObject) obj;

        given().
                headers("Authorization", authorization).
                log().all().
                body(editData).
                when().
                patch(path + "repos/" + ownerName + "/" + repoName).
                then().
                assertThat().
                statusCode(200).
                and().
                body("name", response -> equalTo(repoName)).
                body("description", response -> equalTo("This is your second repository"));

    }

    @Test
    public void testDeleteRepo() throws EventException {
        given().
                headers("Authorization", authorization).
                log().all().
                when().
                delete(path + "repos/" + ownerName + "/" + repoName).
                then().
                assertThat().
                statusCode(204); //204 No Content

    }

}
