package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

public class ReportFlow {

    /******************* OBSERVER ******************/
    @InitiatingFlow
    public static class ReportManually extends FlowLogic<Void> {
        private final SignedTransaction signedTransaction;
        private final Party regulator;

        public ReportManually(SignedTransaction signedTransaction, Party regulator) {
            this.signedTransaction = signedTransaction;
            this.regulator = regulator;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            System.out.println("ReportManually flow start");
            FlowSession session = initiateFlow(regulator);
            session.send(signedTransaction);
            System.out.println("ReportManually flow end");
            return null;
        }
    }

    @InitiatedBy(ReportManually.class)
    public static class ReportManuallyResponder extends FlowLogic<Void> {
        private final FlowSession session;

        public ReportManuallyResponder(FlowSession session) {
            this.session = session;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = session.receive(SignedTransaction.class).unwrap(it -> it);
            // The national regulator records all of the transaction's states using
            // `recordTransactions` with the `ALL_VISIBLE` flag.
            getServiceHub().recordTransactions(StatesToRecord.ALL_VISIBLE, ImmutableList.of(signedTransaction));
            return null;
        }
    }
}
