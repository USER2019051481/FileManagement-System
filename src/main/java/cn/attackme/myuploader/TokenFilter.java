package cn.attackme.myuploader;

import io.jsonwebtoken.Claims;
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

@Component
public class TokenFilter extends OncePerRequestFilter {

    public static final long expirationTime = 1000 * 24 * 60 * 60 * 7;

    private final String secretKey;

    public TokenFilter(@Value("${spring.security.user.password}") String secretKey) {
        this.secretKey = secretKey;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {
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
        System.out.println(secretKey);
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Boolean validateToken(String token) {
        if(Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)!=null)
            return true;
        else {return false;}
    }

    public String generateToken(String hospitalName) {
        return Jwts.builder()
                .setSubject(hospitalName)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}

