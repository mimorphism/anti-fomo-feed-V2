
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
    "title",
    "url",
    "timestamp",
    "description",
    "thumbnail",
    "images",
    "fields",
    "color"
})
@Generated("jsonschema2pojo")
public class Embed {

    @JsonProperty("title")
    private String title;
    @JsonProperty("url")
    private String url;
    @JsonProperty("timestamp")
    private Object timestamp;
    @JsonProperty("description")
    private String description;
    @JsonProperty("thumbnail")
    private Thumbnail thumbnail;
    @JsonProperty("images")
    private List<Object> images = null;
    @JsonProperty("fields")
    private List<Object> fields = null;
    @JsonProperty("color")
    private String color;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("timestamp")
    public Object getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("thumbnail")
    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    @JsonProperty("thumbnail")
    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    @JsonProperty("images")
    public List<Object> getImages() {
        return images;
    }

    @JsonProperty("images")
    public void setImages(List<Object> images) {
        this.images = images;
    }

    @JsonProperty("fields")
    public List<Object> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(List<Object> fields) {
        this.fields = fields;
    }

    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    @JsonProperty("color")
    public void setColor(String color) {
        this.color = color;
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
