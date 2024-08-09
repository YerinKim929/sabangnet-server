package com.daou.sabangnetserver.domain.auth.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LookUpHttpHeader {

    public String getIpAddress(HttpServletRequest request){

        String ip = request.getHeader("X-Forwarded-For");

        if(ip != null && !ip.isEmpty()){
            ip = ip.split(",")[0].trim();
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public String getLoginDeviceInfo(HttpServletRequest request){
        String agent = request.getHeader("User-Agent");
        String browser = null;
        if (agent != null) {
            if (agent.contains("Trident")) {
                browser = "MSIE";
            } else if (agent.contains("Chrome")) {
                browser = "Chrome";
            } else if (agent.contains("Opera")) {
                browser = "Opera";
            } else if (agent.contains("iPhone") && agent.contains("Mobile")) {
                browser = "iPhone";
            } else if (agent.contains("Android") && agent.contains("Mobile")) {
                browser = "Android";
            }
            else
                browser = "";
        }
        return agent + " " + browser;
    }

}
