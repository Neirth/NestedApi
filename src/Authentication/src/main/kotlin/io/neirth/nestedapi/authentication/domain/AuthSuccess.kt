package io.neirth.nestedapi.authentication.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthSuccess(@JsonProperty("access_token")
                        val accessToken : String,

                       @JsonProperty("token_type")
                        val tokenType: String,

                       @JsonProperty("expires_in")
                        val expiresIn: Long,

                       @JsonProperty("refresh_token")
                        val refreshToken: String)