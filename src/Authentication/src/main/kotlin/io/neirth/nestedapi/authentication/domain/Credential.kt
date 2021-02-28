package io.neirth.nestedapi.authentication.domain

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Credentials")
data class Credential(@Id
                      val userId: Long,
                      @Id
                      val password: String)
