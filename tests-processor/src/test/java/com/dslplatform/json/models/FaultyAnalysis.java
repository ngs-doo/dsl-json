package com.dslplatform.json.models;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ServerCapabilities")
@com.dslplatform.json.CompiledJson
public class FaultyAnalysis {
    private @Valid List<Long> mandatoryCapability;
    private @Valid List<Long> optionalCapability;
    private @Valid List<String> serverName;

    public FaultyAnalysis mandatoryCapability(List<Long> mandatoryCapability) {
        this.mandatoryCapability = mandatoryCapability;
        return this;
    }

    @JsonProperty("mandatoryCapability")
    @Size(min=0)
    public List<Long> getMandatoryCapability() {
        return mandatoryCapability;
    }

    @JsonProperty("mandatoryCapability")
    public void setMandatoryCapability(List<Long> mandatoryCapability) {
        this.mandatoryCapability = mandatoryCapability;
    }

    public FaultyAnalysis addMandatoryCapabilityItem(Long mandatoryCapabilityItem) {
        if (this.mandatoryCapability == null) {
            this.mandatoryCapability = new ArrayList<>();
        }

        this.mandatoryCapability.add(mandatoryCapabilityItem);
        return this;
    }

    public FaultyAnalysis removeMandatoryCapabilityItem(Long mandatoryCapabilityItem) {
        if (mandatoryCapabilityItem != null && this.mandatoryCapability != null) {
            this.mandatoryCapability.remove(mandatoryCapabilityItem);
        }

        return this;
    }

    public FaultyAnalysis optionalCapability(List<Long> optionalCapability) {
        this.optionalCapability = optionalCapability;
        return this;
    }

    @JsonProperty("optionalCapability")
    @Size(min=0)
    public List<Long> getOptionalCapability() {
        return optionalCapability;
    }

    @JsonProperty("optionalCapability")
    public void setOptionalCapability(List<Long> optionalCapability) {
        this.optionalCapability = optionalCapability;
    }

    public FaultyAnalysis addOptionalCapabilityItem(Long optionalCapabilityItem) {
        if (this.optionalCapability == null) {
            this.optionalCapability = new ArrayList<>();
        }

        this.optionalCapability.add(optionalCapabilityItem);
        return this;
    }

    public FaultyAnalysis removeOptionalCapabilityItem(Long optionalCapabilityItem) {
        if (optionalCapabilityItem != null && this.optionalCapability != null) {
            this.optionalCapability.remove(optionalCapabilityItem);
        }

        return this;
    }

    public FaultyAnalysis serverName(List<String> serverName) {
        this.serverName = serverName;
        return this;
    }

    @JsonProperty("serverName")
    @Size(min=0)
    public List<String> getServerName() {
        return serverName;
    }

    @JsonProperty("serverName")
    public void setServerName(List<String> serverName) {
        this.serverName = serverName;
    }

    public FaultyAnalysis addServerNameItem(String serverNameItem) {
        if (this.serverName == null) {
            this.serverName = new ArrayList<>();
        }

        this.serverName.add(serverNameItem);
        return this;
    }

    public FaultyAnalysis removeServerNameItem(String serverNameItem) {
        if (serverNameItem != null && this.serverName != null) {
            this.serverName.remove(serverNameItem);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FaultyAnalysis serverCapabilities = (FaultyAnalysis) o;
        return Objects.equals(this.mandatoryCapability, serverCapabilities.mandatoryCapability) &&
               Objects.equals(this.optionalCapability, serverCapabilities.optionalCapability) &&
               Objects.equals(this.serverName, serverCapabilities.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mandatoryCapability, optionalCapability, serverName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServerCapabilities {\n");

        sb.append("    mandatoryCapability: ").append(toIndentedString(mandatoryCapability)).append("\n");
        sb.append("    optionalCapability: ").append(toIndentedString(optionalCapability)).append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

