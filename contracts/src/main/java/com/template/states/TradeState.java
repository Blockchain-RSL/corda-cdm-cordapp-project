package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.ProposalAndTradeContract;
import com.template.enums.TradeStatus;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Date;
import java.util.List;

@BelongsToContract(ProposalAndTradeContract.class)
public class TradeState implements LinearState/*, QueryableState*/ {

    private Party proposer;
    private Party proposee;

    private UniqueIdentifier linearId;

    private String instrumentType;
    private String instrument;

    private String quantity;
    private Double price;

    private String currency;
    private String market;

    private String contractualDefinition;
    private String masterAgreement;

    private TradeStatus tradeStatus;

    private String cdmJsonBase64;
    private Date timestamp;

    @ConstructorForDeserialization
    public TradeState(
            Party proposer, Party proposee,
            UniqueIdentifier linearId,
            String instrumentType, String instrument, String quantity,
            Double price, String currency, String market,
            String contractualDefinition, String masterAgreement,
            TradeStatus tradeStatus,
            String cdmJsonBase64, Date timestamp) {
        this.proposer = proposer;
        this.proposee = proposee;
        this.linearId = linearId;
        this.instrumentType = instrumentType;
        this.instrument = instrument;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.market = market;
        this.contractualDefinition = contractualDefinition;
        this.masterAgreement = masterAgreement;
        this.tradeStatus = tradeStatus;
        this.cdmJsonBase64 = cdmJsonBase64;
        this.timestamp = timestamp;
    }

    public TradeState(
            Party proposer, Party proposee,
            String instrumentType, String instrument, String quantity,
            Double price, String currency, String market,
            String contractualDefinition, String masterAgreement,
            TradeStatus tradeStatus,
            String cdmJsonBase64, Date timestamp) {
        this.proposer = proposer;
        this.proposee = proposee;
        this.linearId = new UniqueIdentifier();
        this.instrumentType = instrumentType;
        this.instrument = instrument;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.market = market;
        this.contractualDefinition = contractualDefinition;
        this.masterAgreement = masterAgreement;
        this.tradeStatus = tradeStatus;
        this.cdmJsonBase64 = cdmJsonBase64;
        this.timestamp = timestamp;
    }

    public Party getProposer() {
        return proposer;
    }

    public Party getProposee() {
        return proposee;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
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

    public String getCdmJsonBase64() {
        return cdmJsonBase64;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getContractualDefinition() {
        return contractualDefinition;
    }

    public String getMasterAgreement() {
        return masterAgreement;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(proposer,proposee);
    }
/*
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof CDMSchema) {
            return new CDMSchema.CDMEntity(
                    this.linearId.getId(),
                    this.instrumentType,
                    this.instrument,
                    this.quantity,
                    this.price,
                    this.currency,
                    this.market,
                    this.tradeStatus.name());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new CDMSchema());
    }
*/


    @Override
    public String toString() {
        return String.format("ProposalState (" +
                        "proposer=%s, proposee=%s, linearId=%s" +
                        "instrumentType=%s, instrument=%s, quantity=%s, " +
                        "price=%s, currency=%s, market=%s, tradeStatus=%s " +
                        ")"
                , proposer, proposee, linearId,
                instrumentType,  instrument, quantity,
                price, currency, market, tradeStatus.name());
    }
}
