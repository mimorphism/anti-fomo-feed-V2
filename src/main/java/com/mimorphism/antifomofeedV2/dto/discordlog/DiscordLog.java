
package com.mimorphism.antifomofeedV2.dto.discordlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "guild",
    "channel",
    "dateRange",
    "messages",
    "messageCount"
})
@Generated("jsonschema2pojo")
public class DiscordLog {

    @JsonProperty("guild")
    private Guild guild;
    @JsonProperty("channel")
    private Channel channel;
    @JsonProperty("dateRange")
    private DateRange dateRange;
    @JsonProperty("messages")
    private List<Message> messages = null;
    @JsonProperty("messageCount")
    private Integer messageCount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("guild")
    public Guild getGuild() {
        return guild;
    }

    @JsonProperty("guild")
    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    @JsonProperty("channel")
    public Channel getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @JsonProperty("dateRange")
    public DateRange getDateRange() {
        return dateRange;
    }

    @JsonProperty("dateRange")
    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    @JsonProperty("messages")
    public List<Message> getMessages() {
        return messages;
    }

    @JsonProperty("messages")
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @JsonProperty("messageCount")
    public Integer getMessageCount() {
        return messageCount;
    }

    @JsonProperty("messageCount")
    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
