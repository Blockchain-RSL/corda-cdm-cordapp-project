package com.template.webserver.enums;

import java.util.Arrays;
import java.util.Optional;

public enum NodeNameEnum {
    PARTY_A("PartyA"),
    PARTY_B("PartyB"),
    PARTY_C("PartyC"),
    NOTARY("Notary"),
    OBSERVER("Observer"),
    UNKNOWN("Unknown");

    private String nodeName;

    NodeNameEnum(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public static NodeNameEnum getEnumByNodeName(String nodeName) {
        Optional<NodeNameEnum> optNodeName = Arrays.stream(NodeNameEnum.values()).filter(nodeNameEnum -> {
                    return nodeNameEnum.getNodeName().equals(nodeName);
                })
                .findFirst();

        return optNodeName.isPresent() ? optNodeName.get() : NodeNameEnum.UNKNOWN;
    }
}
