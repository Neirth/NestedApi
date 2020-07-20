/*
 * MIT License
 *
 * Copyright (c) 2020 NestedApi Project
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
package io.neirth.nestedapi.Authentication.Templates;

import java.util.Date;

/**
 * User Template Class for encapsulate inside a object the user information
 * present in the database.
 */
public class User {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String telephone;
    private Date birthday;
    private Country country;
    private String address;
    private String addressInformation;

    /**
     * Builder class for made a User object within setters methods.
     * 
     * This above all will help us to maintain a cleaner code, since all the fields
     * are mandatory in the constructor of the object.
     */
    public static class Builder {
        private Long id;
        private String name;
        private String surname;
        private String email;
        private String password;
        private String telephone;
        private Date birthday;
        private Country country;
        private String address;
        private String addressInformation;

        public Builder(Long id) {
            this.id = id;
        }

        public Builder setName(String name) {
            this.name = name;

            return this;
        }

        public Builder setSurname(String surname) {
            this.surname = surname;

            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;

            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;

            return this;
        }

        public Builder setTelephone(String telephone) {
            this.telephone = telephone;

            return this;
        }

        public Builder setBirthday(Date birthday) {
            this.birthday = birthday;

            return this;
        }

        public Builder setCountry(Country country) {
            this.country = country;

            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;

            return this;
        }

        public Builder setAddressInformation(String addressInformation) {
            this.addressInformation = addressInformation;

            return this;
        }

        public User build() {
            return new User(this.id, this.name, this.surname, this.email, this.password, this.telephone, this.birthday,
                    this.country, this.address, this.addressInformation);
        }
    }

    private User(Long id, String name, String surname, String email, String password, String telephone, Date birthday,
            Country country, String address, String addressInformation) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.birthday = birthday;
        this.country = country;
        this.address = address;
        this.addressInformation = addressInformation;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getTelephone() {
        return telephone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Country getCountry() {
        return country;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressInformation() {
        return addressInformation;
    }
}