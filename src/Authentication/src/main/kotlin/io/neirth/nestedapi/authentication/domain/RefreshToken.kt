package io.neirth.nestedapi.authentication.domain

import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "RefreshTokens")
data class RefreshToken(@Id
                        val userId: Long,
                        @Id
                        val refreshToken: String,
                        val validFrom: Timestamp)

