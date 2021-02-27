package io.neirth.nestedapi.domain

import java.sql.Timestamp

data class User(val id: Long, val name: String, val surname: String,
                val email: String, val password: String, val telephone: String,
                val birthday: Timestamp, val country: Country, val address: String,
                val addressInformation: String)

