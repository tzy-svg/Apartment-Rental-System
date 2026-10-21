package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static SecretKey secretKey = Keys.hmacShaKeyFor("byDJwyKAQwThKptwUn8Jh7SW3j3EfRHQ".getBytes());

    public static String createToken(Long userId,String username){
        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .setSubject("LOGIN_USER")
                .claim("userId", userId)
                .claim("username", username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }
public static Claims parseToken(String token){
        try {
            JwtParser build = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = build.parseClaimsJws(token);
            Claims body = claimsJws.getBody();
            return body;
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }
        catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

}

    public static void main(String[] args) {
        System.out.println(createToken(8L,"12345678910"));
    }
}
