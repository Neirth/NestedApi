package io.neirth.nestedapi.domain

import java.sql.Timestamp
import javax.persistence.*
import java.io.Serializable

@Entity
@Table(name = "Users")
data class User(@Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                val id: Long, val name: String, val surname: String,
                val email: String, val password: String, val telephone: String,
                val birthday: Timestamp, val country: Country, val address: String,
                val addressInformation: String) : Serializable

