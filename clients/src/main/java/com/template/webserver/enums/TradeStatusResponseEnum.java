package com.template.webserver.enums;

import com.template.enums.TradeStatus;

public enum TradeStatusResponseEnum {
    ACCEPTED,
    PROPOSED,
    REJECTED,
    REJECTED_BY_PROPOSER,
    INCOMING,
    COUNTERPROPOSED,
    INCOMING_COUNTERPROPOSAL,
    UNKNOWN;

    public static TradeStatusResponseEnum convertTradeStatusToResponse(TradeStatus tradeStatus) {
        for(TradeStatusResponseEnum response: TradeStatusResponseEnum.values()) {
            if(response.name().equals(tradeStatus.name())) {
                return response;
            }
        }
        return UNKNOWN;
    }
}
