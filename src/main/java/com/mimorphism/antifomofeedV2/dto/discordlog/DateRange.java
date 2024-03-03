
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
    "after",
    "before"
})
@Generated("jsonschema2pojo")
public class DateRange {

    @JsonProperty("after")
    private Object after;
    @JsonProperty("before")
    private Object before;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("after")
    public Object getAfter() {
        return after;
    }

    @JsonProperty("after")
    public void setAfter(Object after) {
        this.after = after;
    }

    @JsonProperty("before")
    public Object getBefore() {
        return before;
    }

    @JsonProperty("before")
    public void setBefore(Object before) {
        this.before = before;
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
