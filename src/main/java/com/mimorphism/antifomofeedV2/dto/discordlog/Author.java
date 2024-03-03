
package com.mimorphism.antifomofeedV2.dto.discordlog;

import java.util.HashMap;
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
    "name",
    "discriminator",
    "nickname",
    "color",
    "isBot",
    "avatarUrl"
})
@Generated("jsonschema2pojo")
public class Author {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("discriminator")
    private String discriminator;
    @JsonProperty("nickname")
    private String nickname;
    @JsonProperty("color")
    private Object color;
    @JsonProperty("isBot")
    private Boolean isBot;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("discriminator")
    public String getDiscriminator() {
        return discriminator;
    }

    @JsonProperty("discriminator")
    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    @JsonProperty("nickname")
    public String getNickname() {
        return nickname;
    }

    @JsonProperty("nickname")
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @JsonProperty("color")
    public Object getColor() {
        return color;
    }

    @JsonProperty("color")
    public void setColor(Object color) {
        this.color = color;
    }

    @JsonProperty("isBot")
    public Boolean getIsBot() {
        return isBot;
    }

    @JsonProperty("isBot")
    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    @JsonProperty("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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
