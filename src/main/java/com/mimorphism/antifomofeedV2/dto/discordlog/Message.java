
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
    "id",
    "type",
    "timestamp",
    "timestampEdited",
    "callEndedTimestamp",
    "isPinned",
    "content",
    "author",
    "attachments",
    "embeds",
    "stickers",
    "reactions",
    "mentions"
})
@Generated("jsonschema2pojo")
public class Message {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("timestampEdited")
    private Object timestampEdited;
    @JsonProperty("callEndedTimestamp")
    private Object callEndedTimestamp;
    @JsonProperty("isPinned")
    private Boolean isPinned;
    @JsonProperty("content")
    private String content;
    @JsonProperty("author")
    private Author author;
    @JsonProperty("attachments")
    private List<Object> attachments = null;
    @JsonProperty("embeds")
    private List<Embed> embeds = null;
    @JsonProperty("stickers")
    private List<Object> stickers = null;
    @JsonProperty("reactions")
    private List<Object> reactions = null;
    @JsonProperty("mentions")
    private List<Object> mentions = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("timestampEdited")
    public Object getTimestampEdited() {
        return timestampEdited;
    }

    @JsonProperty("timestampEdited")
    public void setTimestampEdited(Object timestampEdited) {
        this.timestampEdited = timestampEdited;
    }

    @JsonProperty("callEndedTimestamp")
    public Object getCallEndedTimestamp() {
        return callEndedTimestamp;
    }

    @JsonProperty("callEndedTimestamp")
    public void setCallEndedTimestamp(Object callEndedTimestamp) {
        this.callEndedTimestamp = callEndedTimestamp;
    }

    @JsonProperty("isPinned")
    public Boolean getIsPinned() {
        return isPinned;
    }

    @JsonProperty("isPinned")
    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(Author author) {
        this.author = author;
    }

    @JsonProperty("attachments")
    public List<Object> getAttachments() {
        return attachments;
    }

    @JsonProperty("attachments")
    public void setAttachments(List<Object> attachments) {
        this.attachments = attachments;
    }

    @JsonProperty("embeds")
    public List<Embed> getEmbeds() {
        return embeds;
    }

    @JsonProperty("embeds")
    public void setEmbeds(List<Embed> embeds) {
        this.embeds = embeds;
    }

    @JsonProperty("stickers")
    public List<Object> getStickers() {
        return stickers;
    }

    @JsonProperty("stickers")
    public void setStickers(List<Object> stickers) {
        this.stickers = stickers;
    }

    @JsonProperty("reactions")
    public List<Object> getReactions() {
        return reactions;
    }

    @JsonProperty("reactions")
    public void setReactions(List<Object> reactions) {
        this.reactions = reactions;
    }

    @JsonProperty("mentions")
    public List<Object> getMentions() {
        return mentions;
    }

    @JsonProperty("mentions")
    public void setMentions(List<Object> mentions) {
        this.mentions = mentions;
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
