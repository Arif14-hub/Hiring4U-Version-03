package com.hiring4u.entity;
import jakarta.persistence.*;
import com.hiring4u.Enums.Role;

@Entity
@Table(name = "Recruitors")
public class RecEntity {

    @Enumerated(EnumType.STRING)
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String companyName;
    @Column(unique = true)
    private String email;
    private String password;

    private String HrName;
    private String HrPhone;
    private String weblink;
    private String HrLocation;

    private String industry;

    private String companySize;

    private String companyAddress;

    @Column(length = 3000)
    private String companyDescription;

    private String linkedInUrl;


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

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    public String getHrPhone() {
        return HrPhone;
    }

    public void setHrPhone(String hrPhone) {
        HrPhone = hrPhone;
    }

    public String getHrLocation() {
        return HrLocation;
    }

    public void setHrLocation(String hrLocation) {
        HrLocation = hrLocation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }
}
