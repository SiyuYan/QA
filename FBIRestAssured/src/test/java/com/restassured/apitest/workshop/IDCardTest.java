package com.restassured.apitest.workshop;

import org.junit.Test;
import org.w3c.dom.events.EventException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class IDCardTest {
    private static String path = "http://apis.juhe.cn/idcard/";
    private static String key = "fee99f40647df279fc73dd05bbb2c6fa";
    private static String cardNumber = "330326198903081212";

    @Test
    public void testGetInformationUsingIDCard() throws EventException {
        given().
                param("key", key).
                param("cardno", cardNumber).
                log().all().
                when().
                get(path + "index").
                then().
                assertThat().
                statusCode(200).
                and().
                body("result.area", response -> containsString("11江"));
    }
}
