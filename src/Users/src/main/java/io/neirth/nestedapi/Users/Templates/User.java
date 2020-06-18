package io.neirth.nestedapi.Users.Templates;
import java.util.Date;

public class User {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String telephone;
    private Date birthday;
    private Country country;
    private String address;
    private String addressInformation;

    public User(String name, String surname, String email, String password, String telephone, Date birthday,
                Country country, String address, String addressInformation) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressInformation() {
        return addressInformation;
    }

    public void setAddressInformation(String addressInformation) {
        this.addressInformation = addressInformation;
    }

}