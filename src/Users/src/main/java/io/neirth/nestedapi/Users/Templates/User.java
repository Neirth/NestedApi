package io.neirth.nestedapi.Users.Templates;
import java.util.Date;

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
            return new User(this.id,
                            this.name, 
                            this.surname, 
                            this.email, 
                            this.password, 
                            this.telephone, 
                            this.birthday, 
                            this.country, 
                            this.address, 
                            this.addressInformation);
        }
    }

    public User(Long id, String name, String surname, String email, String password, String telephone, Date birthday,
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