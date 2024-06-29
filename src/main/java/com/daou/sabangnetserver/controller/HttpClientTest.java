package com.daou.sabangnetserver.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpClientTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 로그 확인용

    public HttpClientTest(String url) {
        Map<String, String> header = new HashMap<>();
        JSONObject postField = new JSONObject();

        String getResult = sendGet(url, header);

        String getPost = sendPost(url, header, postField);

        // JSON 만들기 예제 (getPost가 json 형식이 아니라서 에러 발생)
        //JSONObject jsonObject = new JSONObject(getPost); // 결과값이 JSONObject이라면 JSONObject 형태로 반환
        //JSONArray jsonArray = new JSONArray(getPost); // 결과값이 JSONArray이라면 JSONArray 형태로 반환

    }

    /**
     * HTTP 통신 (get 방식)
     */
    public String sendGet(String url, Map<String, String> header) {

        String result = null;

        try {
            HttpClient client = HttpClientBuilder.create().build();              // HttpClient 생성
            HttpGet getRequest = new HttpGet(url);                               // URL(GET)

            // 헤더 세팅
            if (header != null) {
                for(String strKey : header.keySet()) {
                    getRequest.addHeader(strKey, header.get(strKey));
                }
            }

            HttpResponse httpResponse =  client.execute(getRequest);               // 통신 실행

            //Apache에서 제공하는 EntityUtils 객체를 통해 응답 Entity를 문자열로 치환할 수 있습니다.
            result = EntityUtils.toString(httpResponse.getEntity());

            logger.info("[Request][GET] URL: {}", url);
            logger.info("[Request][GET] Header: {}", header);
            logger.info("[Response][GET] Body: {}", result);

        } catch (Exception e){
            logger.error(e.getMessage());
        }

        return result;
    }


    /**
     * HTTP 통신 (post 방식)
     */
    public String sendPost(String url, Map<String, String> header, JSONObject postField) {

        String result = null;

        try {
            HttpClient client = HttpClientBuilder.create().build();                 // HttpClient 생성
            HttpPost postRequest = new HttpPost(url);                               // URL(POST)

            // Header 세팅
            if (header != null) {
                for(String strKey : header.keySet()) {
                    postRequest.addHeader(strKey, header.get(strKey));
                }
            }

            postRequest.setEntity(new StringEntity(postField.toString()));         // POST DATA 세팅
            HttpResponse httpResponse = client.execute(postRequest);               // 통신 실행

            //Apache에서 제공하는 EntityUtils 객체를 통해 응답 Entity를 문자열로 치환할 수 있습니다.
            result = EntityUtils.toString(httpResponse.getEntity());

            logger.info("[Response][POST] URL: {}", url);
            logger.info("[Response][POST] Header: {}", header);
            logger.info("[Response][POST] Body: {}", postField);
            logger.info("[Response][POST] Body: {}", result);

        } catch (Exception e){
            logger.error(e.getMessage());
        }

        return result;
    }
}
