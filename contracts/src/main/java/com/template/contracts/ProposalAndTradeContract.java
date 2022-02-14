package com.template.contracts;


import com.google.common.collect.ImmutableSet;
import com.template.enums.TradeStatus;
import com.template.states.TradeState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ProposalAndTradeContract implements Contract {
    public static String ID = "com.template.contracts.ProposalAndTradeContract";
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties command = tx.getCommands().get(0);

        if( command.getValue() instanceof  Commands.Propose) {
            requireThat(require -> {
                require.using("There are no inputs", tx.getInputs().isEmpty());
                require.using("Only one output state should be created.", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);
                //require.using("The buyer and seller are the proposer and the proposee", ImmutableSet.of(output.getProposer(), output.getProposee()).equals(ImmutableSet.of(output.getProposee(), output.getProposer())));
                require.using("The proposer is a required signer", command.getSigners().contains(output.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(output.getProposee().getOwningKey()));
                return null;
            });
        }else if(command.getValue() instanceof Commands.Modification){
            requireThat(require -> {
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type TradeState", tx.inputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                TradeState input = tx.inputsOfType(TradeState.class).get(0);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);

                require.using("The request must be a proposal or a counterproposal",
                        input.getTradeStatus() == TradeStatus.PROPOSED || input.getTradeStatus() == TradeStatus.COUNTERPROPOSED);

                require.using("The buyer is unmodified in the output", input.getProposer().equals(output.getProposer()));
                require.using("The seller is unmodified in the output", input.getProposee().equals(output.getProposee()));

                require.using("The proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));
                return null;
            });
        }/*else if(command.getValue() instanceof Commands.Modification){
            requireThat(require -> {
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type TradeState", tx.inputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                TradeState input = tx.inputsOfType(TradeState.class).get(0);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);

                require.using("The request must be a proposal", input.getTradeStatus().equals(TradeStatus.PROPOSED));

                require.using("The buyer is unmodified in the output", input.getProposer().equals(output.getProposer()));
                require.using("The seller is unmodified in the output", input.getProposee().equals(output.getProposee()));

                require.using("The proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));
                return null;
            });
        }*/
        else if(command.getValue() instanceof Commands.Accept){
            requireThat(require -> {
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type TradeState", tx.inputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                TradeState input = tx.inputsOfType(TradeState.class).get(0);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);

                require.using("The request must be a proposal", input.getTradeStatus().equals(TradeStatus.PROPOSED));

                require.using("The buyer is unmodified in the output", input.getProposer().equals(output.getProposer()));
                require.using("The seller is unmodified in the output", input.getProposee().equals(output.getProposee()));

                require.using("The proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));
                return null;
            });
        }else if(command.getValue() instanceof Commands.Reject){
            requireThat(require ->{
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type TradeState", tx.inputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                TradeState input = tx.inputsOfType(TradeState.class).get(0);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);

                require.using("The request must be a proposal or a counterproposal",
                        input.getTradeStatus() == TradeStatus.PROPOSED || input.getTradeStatus() == TradeStatus.COUNTERPROPOSED);
                require.using("The buyer is unmodified in the output", input.getProposer().equals(output.getProposer()));
                require.using("The seller is unmodified in the output", input.getProposee().equals(output.getProposee()));

                require.using("The proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));
                return null;
            });
        }else if(command.getValue() instanceof Commands.Counterpropose){
            requireThat(require -> {
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type TradeState", tx.inputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type TradeState", tx.outputsOfType(TradeState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                TradeState input = tx.inputsOfType(TradeState.class).get(0);
                TradeState output = tx.outputsOfType(TradeState.class).get(0);

                require.using("The request must be a proposal", input.getTradeStatus().equals(TradeStatus.PROPOSED));

                require.using("The buyer is unmodified in the output", input.getProposer().equals(output.getProposer()));
                require.using("The seller is unmodified in the output", input.getProposee().equals(output.getProposee()));

                require.using("The proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));
                return null;
            });
        }else{
            throw new IllegalArgumentException("Command of incorrect type");
        }

    }


    public interface Commands extends CommandData {
        class Propose implements Commands{};
        class Modification implements Commands{};
        class Accept implements Commands{};
        class Reject implements Commands{};
        class Counterpropose implements Commands{};
    }
}

