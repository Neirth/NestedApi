package io.neirth.nestedapi.Authentication.Templates;

import java.sql.Date;

public class Token {
    private Long userId;
    private Date validFrom;
    private String token;
    private String userAgent; 

    public static class Builder {
        private Long userId;
        private Date validFrom;
        private String token;
        private String userAgent; 

        public Builder setUserId(Long userId) {
            this.userId = userId;

            return this;
        }

        public Builder setValidFrom(Date validFrom) {
            this.validFrom = validFrom;
            
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;

            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;

            return this;
        }

        public Token build() {
            return new Token(userId, token, validFrom, userAgent);
        }
    }

    private Token(Long userId, String token, Date validFrom, String userAgent) {
        this.userId = userId;
        this.token = token;
        this.validFrom = validFrom;
        this.userAgent = userAgent;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public String getUserAgent() {
        return userAgent;
    }
}