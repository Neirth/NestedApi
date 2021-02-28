package io.neirth.nestedapi.authentication.domain

import javax.persistence.Id

data class Credential(@Id
                      val userId: Long,
                      @Id
                      val password: String)
