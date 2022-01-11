package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.template.contracts.ProposalAndTradeContract;
import com.template.enums.TradeStatus;
import com.template.states.TradeState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

public class ProposalFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<UniqueIdentifier> {

        private Party counterparty;
        private Party observer;
        private String instrumentType;
        private String instrument;
        private String quantity;
        private Double price;
        private String currency;
        private String market;
        private String contractualDefinition;
        private String masterAgreement;
        private String cdmJsonBase64;

        public Initiator(
                Party counterparty, Party observer,
                String instrumentType, String instrument, String quantity,
                Double price, String currency, String market,
                String contractualDefinition, String masterAgreement,
                String cdmJsonBase64) {

            this.counterparty = counterparty;
            this.observer = observer;
            this.instrumentType = instrumentType;
            this.instrument = instrument;
            this.quantity = quantity;
            this.price = price;
            this.currency = currency;
            this.contractualDefinition = contractualDefinition;
            this.masterAgreement = masterAgreement;
            this.market = market;
            this.cdmJsonBase64 = cdmJsonBase64;
        }

        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            Party buyer, seller;


            TradeState output = new TradeState(
                    getOurIdentity(), counterparty,
                    instrumentType, instrument, quantity,
                    price, currency, market,
                    contractualDefinition, masterAgreement,
                    TradeStatus.PROPOSED,
                    cdmJsonBase64, new Date());

            //Creating the command
            ProposalAndTradeContract.Commands.Propose commandType = new ProposalAndTradeContract.Commands.Propose();
            List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), counterparty.getOwningKey());
            Command command = new Command(commandType, requiredSigners);


            /******************** ATTACHMENT START ***************/
            // attachment management
            // upload attachment
            /*String path = "D:/Corda/nssm-2.24.zip";
            SecureHash attachmentHash = null;
            try {
                attachmentHash = SecureHash.parse(uploadAttachment(
                        path,
                        getServiceHub(),
                        getOurIdentity(),
                        "nssm")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(MessageFormat.format("attachmentHash : %s", attachmentHash));*/
            /****************** ATTACHMENT END ***************/


            //Building the transaction
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command)
                    //.addAttachment(attachmentHash)
                    ;

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            System.out.println("Init ctp flow");
            FlowSession sessionCtp = initiateFlow(counterparty);
            System.out.println("Init observer flow");
            FlowSession sessionsObs = initiateFlow(observer);

            System.out.println("Start request subflow collect");
            //Gather counterparty sigs
            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(sessionCtp)));

            System.out.println("Finalize ctp");
            //Finalise the transaction
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(fullyStx, ImmutableList.of(sessionCtp, sessionsObs)));

            System.out.println("Finalize report");
            // We also distribute the transaction to the national regulator manually.
            subFlow(new ReportFlow.ReportManually(partStx, observer));

            System.out.println("Return proposal");
            return finalisedTx.getTx().outputsOfType(TradeState.class).get(0).getLinearId();
        }
    }

    private static String uploadAttachment(String path, ServiceHub service, Party whoami, String filename) throws IOException {
        SecureHash attachmentHash = service.getAttachments().importAttachment(
                new FileInputStream(new File(path)),
                whoami.toString(),
                filename
        );

        return attachmentHash.toString();
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

            System.out.println("Responder flow start");
            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession){

                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();

            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            System.out.println("Responder flow finish");
            return finalisedTx;
        }
    }

}
