package com.template.webserver.dto;

import net.corda.core.identity.Party;

public class NodeInfoDTO {

    private String name;
    private String city;
    private String country;
    private boolean status;

    public NodeInfoDTO(Party party, boolean isNodeUp) {
        this.name = party.getName().getOrganisation();
        this.city = party.getName().getLocality();
        this.country = party.getName().getCountry();
        this.status = isNodeUp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
