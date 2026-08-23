package com.hiring4u.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecruiterDTO {

    private long id;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Email(message = "Enter a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    private String HrName;
    private String HrPhone;
    private String weblink;
    private String HrLocation;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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


    public String getHrName() {
        return HrName;
    }

    public void setHrName(String hrName) {
        HrName = hrName;
    }

    public String getHrPhone() {
        return HrPhone;
    }

    public void setHrPhone(String hrPhone) {
        HrPhone = hrPhone;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    public String getHrLocation() {
        return HrLocation;
    }

    public void setHrLocation(String hrLocation) {
        HrLocation = hrLocation;
    }
}
