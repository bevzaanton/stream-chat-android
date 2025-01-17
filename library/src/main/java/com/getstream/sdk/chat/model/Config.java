package com.getstream.sdk.chat.model;

import androidx.room.TypeConverters;

import com.getstream.sdk.chat.storage.converter.CommandListConverter;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * A config
 */

public class Config {
    @TypeConverters({DateConverter.class})
    @SerializedName("created_at")
    private Date created_at;

    @SerializedName("updated_at")
    @TypeConverters({DateConverter.class})
    private Date updated_at;
    @SerializedName("name")
    private String name;
    @SerializedName("typing_events")
    private boolean typingEvents;
    @SerializedName("read_events")
    private boolean readEvents;
    @SerializedName("connect_events")
    private boolean connect_events;
    @SerializedName("search")
    private boolean search;
    @SerializedName("reactions")
    private boolean reactionsEnabled;
    @SerializedName("replies")
    private boolean repliesEnabled;
    @SerializedName("mutes")
    private boolean mutes;
    @SerializedName("infinite")
    private String infinite;
    @SerializedName("max_message_length")
    private int max_message_length;
    @SerializedName("automod")
    private String automod;
    @SerializedName("commands")
    @TypeConverters(CommandListConverter.class)
    private List<Command> commands;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReadEvents() {
        return readEvents;
    }

    public void setReadEvents(boolean readEvents) {
        this.readEvents = readEvents;
    }

    public String getInfinite() {
        return infinite;
    }

    public void setInfinite(String infinite) {
        this.infinite = infinite;
    }

    public int getMax_message_length() {
        return max_message_length;
    }

    public void setMax_message_length(int max_message_length) {
        this.max_message_length = max_message_length;
    }

    public String getAutomod() {
        return automod;
    }

    public void setAutomod(String automod) {
        this.automod = automod;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public boolean isTypingEvents() {
        return typingEvents;
    }

    public void setTypingEvents(boolean typingEvents) {
        this.typingEvents = typingEvents;
    }

    public boolean isConnect_events() {
        return connect_events;
    }

    public void setConnect_events(boolean connect_events) {
        this.connect_events = connect_events;
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    public boolean isReactionsEnabled() {
        return reactionsEnabled;
    }

    public void setReactionsEnabled(boolean reactionsEnabled) {
        this.reactionsEnabled = reactionsEnabled;
    }

    public boolean isRepliesEnabled() {
        return repliesEnabled;
    }

    public void setRepliesEnabled(boolean repliesEnabled) {
        this.repliesEnabled = repliesEnabled;
    }

    public boolean isMutes() {
        return mutes;
    }

    public void setMutes(boolean mutes) {
        this.mutes = mutes;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
