package com.template.webserver.dto;

import com.template.states.TradeState;
import com.template.webserver.enums.TradeStatusResponseEnum;
import net.corda.core.identity.Party;

import java.io.Serializable;
import java.util.Date;

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

    private String contractualDefinition;
    private String masterAgreement;

    private String cdmJsonBase64;
    private Date timestamp;

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
        setContractualDefinition(tradeState.getContractualDefinition());
        setMasterAgreement(tradeState.getMasterAgreement());
        setCdmJsonBase64(tradeState.getCdmJsonBase64());
        setTimestamp(tradeState.getTimestamp());

        // convert tradeStatus to a tradeStatus response
        // and check if it s proposed and for me, then it s an incoming status
        TradeStatusResponseEnum tradeStatusResponse =
                TradeStatusResponseEnum.convertTradeStatusToResponse(tradeState.getTradeStatus());

        /*setTradeStatus((tradeStatusResponse == TradeStatusResponseEnum.PROPOSED
                        && tradeState.getProposee().getName().equals(me.getName())) ?
                        TradeStatusResponseEnum.INCOMING : tradeStatusResponse);*/

        if(tradeStatusResponse == TradeStatusResponseEnum.PROPOSED && tradeState.getProposee().getName().equals(me.getName())) {
            tradeStatusResponse = TradeStatusResponseEnum.INCOMING;
        }else if (tradeStatusResponse == TradeStatusResponseEnum.COUNTERPROPOSED
                && tradeState.getProposer().getName().equals(me.getName())) {
            tradeStatusResponse = TradeStatusResponseEnum.INCOMING_COUNTERPROPOSAL;
        }/*else if (tradeStatusResponse == TradeStatusResponseEnum.REJECTED
                && tradeState.getProposee().getName().equals(me.getName())) {
            tradeStatusResponse = TradeStatusResponseEnum.REJECTED_BY_PROPOSER;
        }*/

        setTradeStatus(tradeStatusResponse);
    };

    public String getContractualDefinition() {
        return contractualDefinition;
    }

    public String getMasterAgreement() {
        return masterAgreement;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getCdmJsonBase64() {
        return cdmJsonBase64;
    }

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


    public void setContractualDefinition(String contractualDefinition) {
        this.contractualDefinition = contractualDefinition;
    }

    public void setMasterAgreement(String masterAgreement) {
        this.masterAgreement = masterAgreement;
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

    public void setCdmJsonBase64(String cdmJsonBase64) {
        this.cdmJsonBase64 = cdmJsonBase64;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
