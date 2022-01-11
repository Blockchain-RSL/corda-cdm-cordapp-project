package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.template.contracts.ProposalAndTradeContract;
import com.template.enums.TradeStatus;
import com.template.states.TradeState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.List;

public class ModificationFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private UniqueIdentifier proposalId;
        private Double newPrice;
        private String newQuantity;
        private Party observer;

        private ProgressTracker progressTracker = new ProgressTracker();

        public Initiator(UniqueIdentifier proposalId,
                         Double newPrice, String newQuantity,
                         Party observer) {
            this.proposalId = proposalId;
            this.newPrice = newPrice;
            this.newQuantity = newQuantity;
            this.observer = observer;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            QueryCriteria.LinearStateQueryCriteria inputCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(proposalId), Vault.StateStatus.UNCONSUMED,null);

            StateAndRef inputStateAndRef = getServiceHub().getVaultService().queryBy(TradeState.class, inputCriteria).getStates().get(0);

            TradeState input = (TradeState) inputStateAndRef.getState().getData();

            //Creating the output
            TradeState output = new TradeState(
                    input.getProposer(), input.getProposee(), input.getLinearId(),
                    input.getInstrumentType(), input.getInstrument(), newQuantity,
                    newPrice, input.getCurrency(), input.getMarket(),
                    input.getContractualDefinition(), input.getMasterAgreement(),
                    TradeStatus.PROPOSED,
                    input.getCdmJsonBase64(), new Date());

            //Creating the command
            List<PublicKey> requiredSigners = ImmutableList.of(input.getProposer().getOwningKey(), input.getProposee().getOwningKey());
            Command command = new Command(new ProposalAndTradeContract.Commands.Modification(), requiredSigners);

            //Building the transaction
            Party notary = inputStateAndRef.getState().getNotary();
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addInputState(inputStateAndRef)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            //Gathering the counterparty's signature
            Party counterparty = input.getProposee();

            System.out.println("Init ctp flow");
            FlowSession sessionCtp = initiateFlow(counterparty);
            System.out.println("Init observer flow");
            FlowSession sessionsObs = initiateFlow(observer);

            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(sessionCtp)));

            // Finalising the transaction
            SignedTransaction finalisedTx  = subFlow(new FinalityFlow(fullyStx, ImmutableList.of(sessionCtp, sessionsObs)));

            System.out.println("Finalize report");
            // We also distribute the transaction to the national regulator manually.
            subFlow(new ReportFlow.ReportManually(partStx, observer));

            System.out.println("Return proposal");

            return finalisedTx;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction>{
        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession){

                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                    try {
                        LedgerTransaction ledgerTx = stx.toLedgerTransaction(getServiceHub(), false);
                        Party proposer = ledgerTx.inputsOfType(TradeState.class).get(0).getProposer();
                        if(!proposer.equals(counterpartySession.getCounterparty())){
                            throw new FlowException("Only the proposer can accept a modification.");
                        }
                    } catch (SignatureException e) {
                        throw new FlowException("Check transaction failed");
                    }
                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();
            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}
