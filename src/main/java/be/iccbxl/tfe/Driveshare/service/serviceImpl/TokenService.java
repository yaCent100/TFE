package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new TokenInfo(email, LocalDateTime.now().plusMinutes(30)));
        return token;
    }

    public String validateToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null || tokenInfo.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenStore.remove(token);
            return null;
        }
        return tokenInfo.getEmail();
    }

    private static class TokenInfo {
        private final String email;
        private final LocalDateTime expiryDate;

        public TokenInfo(String email, LocalDateTime expiryDate) {
            this.email = email;
            this.expiryDate = expiryDate;
        }

        public String getEmail() {
            return email;
        }

        public LocalDateTime getExpiryDate() {
            return expiryDate;
        }
    }
}
