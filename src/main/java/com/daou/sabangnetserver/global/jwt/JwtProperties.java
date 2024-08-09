/*application.yml에 작성한 값 매핑*/
package com.daou.sabangnetserver.global.jwt;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@Data
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String header; //yml의 header
    private String secretKey; //yml의 secret
    private long tokenTime; // yml의 token-expiration-in-seconds
}






