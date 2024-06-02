package cn.attackme.myuploader;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenFilter extends OncePerRequestFilter {

    public static final long expirationTime = 1000 * 24 * 60 * 60 * 7;

    private final String secretKey;

    public TokenFilter(@Value("${spring.security.user.password}") String secretKey) {
        this.secretKey = secretKey;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 从header中拿到token
        String token = extractTokenFromRequest(request);
        if (token != null && validateToken(token)) {
            String hospitalName = extractHospitalName(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(hospitalName, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        final String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }


    public String extractHospitalName(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get("hospitalName");  //获取主体信息
    }

    /**
     * 验证一个给定的 JSON Web Token (JWT) 是否有效。
     * @param token
     * @return
     */
    public Boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false; // token has expired
            }
            return true;
        } catch (ExpiredJwtException e) {
            return false; // token has expired
        } catch (Exception e) {
            return false; // token is invalid
        }
    }

    public String generateToken(String hospitalName, String workid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("workid", workid);
        claims.put("hospitalName", hospitalName); // Add hospitalName to the claims map
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}

