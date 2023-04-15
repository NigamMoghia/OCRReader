import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class GetResponseForWebService {
    public static Response response;

    public static void run(String method, String contentType, String postData, Map<String,String> headerMap, String uri, int expectedResponseCode){
        System.setProperty("jsse.enableSNIExtension", "false");
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        if(method.contains("POST") && postData!=null )
            response = given().contentType(contentType).headers(headerMap).when().body(postData).post(uri).then().statusCode(expectedResponseCode).extract().response();
      //  System.out.println("Response received: " + response.getBody().asString());
    }
}
