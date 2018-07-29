package com.acme;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.Serializable;
import java.util.Set;

/**
 *
 */
public class AppUser implements User, Serializable {
    private static final long serializationId = 1L;

    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;
    private String enrollmentSecret;

    public AppUser() {
        // no-arg constructor
    }

    public AppUser(String name, String affiliation, String mspId, Enrollment enrollment) {
        this.name = name;
        this.affiliation = affiliation;
        this.enrollment = enrollment;
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getEnrollmentSecret() {
        return enrollmentSecret;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppUser appUser = (AppUser) o;

        if (name != null ? !name.equals(appUser.name) : appUser.name != null) return false;
        if (roles != null ? !roles.equals(appUser.roles) : appUser.roles != null) return false;
        if (account != null ? !account.equals(appUser.account) : appUser.account != null) return false;
        if (affiliation != null ? !affiliation.equals(appUser.affiliation) : appUser.affiliation != null) return false;
        if (enrollment != null ? !enrollment.equals(appUser.enrollment) : appUser.enrollment != null) return false;
        if (mspId != null ? !mspId.equals(appUser.mspId) : appUser.mspId != null) return false;
        return enrollmentSecret != null ? enrollmentSecret.equals(appUser.enrollmentSecret) : appUser.enrollmentSecret == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (affiliation != null ? affiliation.hashCode() : 0);
        result = 31 * result + (enrollment != null ? enrollment.hashCode() : 0);
        result = 31 * result + (mspId != null ? mspId.hashCode() : 0);
        result = 31 * result + (enrollmentSecret != null ? enrollmentSecret.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AcmeUser{" +
                "name='" + name + '\'' +
                "\n, roles=" + roles +
                "\n, account='" + account + '\'' +
                "\n, affiliation='" + affiliation + '\'' +
                "\n, enrollment=" + enrollment +
                "\n, mspId='" + mspId + '\'' +
                '}';
    }
}
