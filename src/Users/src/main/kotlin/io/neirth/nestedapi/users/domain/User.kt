/*
 * MIT License
 *
 * Copyright (c) 2021 NestedApi Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.users.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @NotNull(message = "The field \"name\" cannot be null")
    @NotEmpty(message = "The field \"name\" cannot be empty")
    var name: String,

    @NotNull(message = "The field \"surname\" cannot be null")
    @NotEmpty(message = "The field \"surname\" cannot be empty")
    var surname: String,

    @Column(unique = true)
    @NotNull(message = "The field \"email\" cannot be null")
    @NotEmpty(message = "The field \"email\" cannot be empty")
    var email: String,

    @Column(unique = true)
    @NotNull(message = "The field \"telephone\" cannot be null")
    @NotEmpty(message = "The field \"telephone\" cannot be empty")
    var telephone: String,

    @NotNull(message = "The field \"birthday\" cannot be null")
    @NotEmpty(message = "The field \"birthday\" cannot be empty")
    var birthday: Timestamp,

    @NotNull(message = "The field \"country\" cannot be null")
    @NotEmpty(message = "The field \"country\" cannot be empty")
    var country: Country,

    @JsonIgnore
    @NotNull(message = "The field \"password\" cannot be null")
    @NotEmpty(message = "The field \"password\" cannot be empty")
    var password: String,

    var address: String,

    var addressInformation: String
)

