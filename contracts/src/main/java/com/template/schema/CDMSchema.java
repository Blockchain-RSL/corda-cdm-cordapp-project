package com.template.schema;

import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;
import com.google.common.collect.ImmutableList;
import org.hibernate.annotations.Type;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@CordaSerializable
public class CDMSchema extends MappedSchema {
    public CDMSchema() {
        super(GenericSchema.class, 1, ImmutableList.of(CDMEntity.class));
    }

    @Entity
    @CordaSerializable
    @Table(name = "cdm_states")
    public static class CDMEntity extends PersistentState {

        @Column(name = "instrument_type")
        private String instrumentType;
        @Column(name = "instrument")
        private String instrument;
        @Column(name = "quantity")
        private String quantity;
        @Column(name = "price")
        private Double price;
        @Column(name = "currency")
        private String currency;
        @Column(name = "market")
        private String market;
        @Column(name = "linear_id")
        @Type(type= "uuid-char")
        private UUID linearId;
        @Column(name = "trade_status")
        private String tradeStatus;


        public CDMEntity(UUID linearId,
                         String instrumentType, String instrument, String quantity,
                         Double price, String currency, String market,
                         String tradeStatus) {
            this.instrumentType = instrumentType;
            this.instrument = instrument;
            this.quantity = quantity;
            this.price = price;
            this.currency = currency;
            this.market = market;
            this.linearId = linearId;
            this.tradeStatus = tradeStatus;
        }

        // Default constructor required by hibernate.
        public CDMEntity() {
            this.instrumentType = null;
            this.instrument = null;
            this.quantity = null;
            this.price = 0.0;
            this.currency = null;
            this.market = null;
            this.linearId = null;
            this.tradeStatus = null;
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

        public UUID getLinearId() {
            return linearId;
        }
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "CordaCDMCordappProject.changelog-master";
    }
}
