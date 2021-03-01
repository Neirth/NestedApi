package io.neirth.nestedapi.authentication.domain

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Credentials")
data class Credential(@Id
                      var userId: Long,
                      @Id
                      var password: String)
