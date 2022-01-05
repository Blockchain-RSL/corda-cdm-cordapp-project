package com.template.webserver.dto;

import com.template.states.TradeState;
import com.template.webserver.enums.TradeStatusResponseEnum;
import net.corda.core.identity.Party;

import java.io.Serializable;

public class CdmStateDTO implements Serializable {

    private String proposer;
    private String proposee;
    private String linearId;
    private String instrumentType;
    private String instrument;
    private String quantity;
    private Double price;
    private String currency;
    private String market;
    private TradeStatusResponseEnum tradeStatus;

    public CdmStateDTO(){};

    public CdmStateDTO(TradeState tradeState, Party me){
        setProposer(tradeState.getProposer().getName().getOrganisation());
        setCurrency(tradeState.getCurrency());
        setInstrument(tradeState.getInstrument());
        setInstrumentType(tradeState.getInstrumentType());
        setMarket(tradeState.getMarket());
        setProposee(tradeState.getProposee().getName().getOrganisation());
        setQuantity(tradeState.getQuantity());
        setPrice(tradeState.getPrice());
        setLinearId(tradeState.getLinearId().getId().toString());

        // convert tradeStatus to a tradeStatus response
        // and check if it s proposed and for me, then it s an incoming status
        TradeStatusResponseEnum tradeStatusResponse =
                TradeStatusResponseEnum.convertTradeStatusToResponse(tradeState.getTradeStatus());
        setTradeStatus(
                (tradeStatusResponse == TradeStatusResponseEnum.PROPOSED
                        && tradeState.getProposee().getName().equals(me.getName())) ?
                        TradeStatusResponseEnum.INCOMING : tradeStatusResponse);
    };

    public String getInstrumentType() {
        return instrumentType;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMarket() {
        return market;
    }

    public String getLinearId() {
        return linearId;
    }

    public String getProposer() {
        return proposer;
    }

    public String getProposee() {
        return proposee;
    }

    public TradeStatusResponseEnum getTradeStatus() {
        return tradeStatus;
    }

    public void setProposer(String proposer) {
        this.proposer = proposer;
    }

    public void setProposee(String proposee) {
        this.proposee = proposee;
    }

    public void setTradeStatus(TradeStatusResponseEnum tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public void setLinearId(String linearId) {
        this.linearId = linearId;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setMarket(String market) {
        this.market = market;
    }

}
