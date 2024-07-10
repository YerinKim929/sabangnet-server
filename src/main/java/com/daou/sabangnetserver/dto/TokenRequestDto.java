package com.daou.sabangnetserver.dto;


public class TokenRequestDto {

    private String sltnCd;
    private String apiKey;

    // Constructors, getters, and setters
    public TokenRequestDto(String apiKey, String sltnCd) {
        this.apiKey = apiKey;
        this.sltnCd = sltnCd;
    }

    // Getters and setters
    public String getSltnCd() {
        return sltnCd;
    }

    public void setSltnCd(String sltnCd) {
        this.sltnCd = sltnCd;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }




}