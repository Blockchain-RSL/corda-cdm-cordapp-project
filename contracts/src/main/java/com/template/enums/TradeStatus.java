package com.template.enums;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum TradeStatus {
    ACCEPTED,
    PROPOSED,
    REJECTED
}
