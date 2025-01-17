package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * A member
 */
public class Member implements UserEntity {
    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("role")
    @Expose
    private String role;

    @SerializedName("created_at")
    @Expose
    private Date createdAt;

    @SerializedName("updated_at")
    @Expose
    private Date updatedAt;

    @SerializedName("invited")
    @Expose
    private boolean invited;

    @SerializedName("invite_accepted_at")
    @Expose
    private Date inviteAcceptedAt;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public Date getInviteAcceptedAt() {
        return inviteAcceptedAt;
    }

    public void setInviteAcceptedAt(Date inviteAcceptedAt) {
        this.inviteAcceptedAt = inviteAcceptedAt;
    }

    @Override
    public String getUserId() {
        if (user == null) return null;
        return user.getId();
    }
}
