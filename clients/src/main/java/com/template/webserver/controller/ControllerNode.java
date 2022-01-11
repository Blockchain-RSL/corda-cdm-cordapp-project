package com.template.webserver.controller;

import com.google.gson.Gson;
import com.template.flows.AcceptanceFlow;
import com.template.flows.ModificationFlow;
import com.template.flows.ProposalFlow;
import com.template.flows.RejectFlow;
import com.template.states.TradeState;
import com.template.webserver.dto.CdmStateDTO;
import com.template.webserver.dto.NodeInfoDTO;
import com.template.webserver.dto.TextMessageDTO;
import com.template.webserver.dto.ValidationJsonDTO;
import com.template.webserver.enums.NodeNameEnum;
import com.template.webserver.rpc.NodeRPCConnectionNode;
import com.template.webserver.rpc.impl.*;
import com.template.webserver.utils.ParsingUtils;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import rx.Observable;
import rx.Subscription;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
//@RequestMapping("/node") // The paths for HTTP requests are relative to this base path.
@RequestMapping
public class ControllerNode {
    private static final Logger logger = LoggerFactory.getLogger(ControllerNode.class);

    /******************* WEBSOCKET START *****************/

    @Autowired
    SimpMessagingTemplate template;
/*
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody List<CdmStateDTO> listCdmMessageDTO) {
        template.convertAndSend("/topic/message", listCdmMessageDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @SendTo("/topic/market")
    public CdmStateDTO broadcastMessage(@Payload TradeState tradeStateUpdate) {
        logger.info("broadcastMessage " + tradeStateUpdate);
        Optional<Party> me = getProxyByNodeName(tradeStateUpdate.getProposer().getName().getOrganisation())
                .networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals(
                        tradeStateUpdate.getProposer().getName().getOrganisation()))
                .findFirst();
        logger.info("send cdm state dto " + new CdmStateDTO(tradeStateUpdate, me.get()));
        return new CdmStateDTO(tradeStateUpdate, me.get());
    }*/

    @SendTo("/topic/message")
    public TextMessageDTO broadcastMessage(@Payload TextMessageDTO textMessageDTO) {
        return textMessageDTO;
    }

    private void updateState(TradeState tradeStateUpdate, Party me) {
        //template.convertAndSend("/topic/message", tradeStateUpdate);

        TextMessageDTO msg = new TextMessageDTO();

        msg.setMessage(new Gson().toJson(new CdmStateDTO(tradeStateUpdate, me)));
        //logger.info("updateState " + msg);
        template.convertAndSend("/topic/message", msg);
    }

    /******************* WEBSOCKET END *****************/

    private NodeRPCConnectionNodeA rpcConnectionNodeA;
    private NodeRPCConnectionNodeB rpcConnectionNodeB;
    private NodeRPCConnectionNodeC rpcConnectionNodeC;
    private NodeRPCConnectionNodeNotary rpcConnectionNodeNotary;
    private NodeRPCConnectionNodeObserver rpcConnectionNodeObserver;

    public ControllerNode(
            NodeRPCConnectionNodeA rpcConnectionNodeA,
            NodeRPCConnectionNodeB rpcConnectionNodeB,
            NodeRPCConnectionNodeC rpcConnectionNodeC,
            NodeRPCConnectionNodeNotary rpcConnectionNodeNotary,
            NodeRPCConnectionNodeObserver rpcConnectionNodeObserver
    ) {

        try {
            this.rpcConnectionNodeA = rpcConnectionNodeA;
            this.rpcConnectionNodeB = rpcConnectionNodeB;
            this.rpcConnectionNodeC = rpcConnectionNodeC;
            this.rpcConnectionNodeNotary = rpcConnectionNodeNotary;
            this.rpcConnectionNodeObserver = rpcConnectionNodeObserver;

            subscribe();
        } catch(Exception exc) {
            logger.error(exc.getMessage());
        }
    }

    private Map<NodeNameEnum, CordaRPCOps> getMapProxy() {
        final Map<NodeNameEnum, CordaRPCOps> mapTmp = new HashMap<>();

        List<NodeRPCConnectionNode> listRpcConnection = new ArrayList<>();
        listRpcConnection.add(rpcConnectionNodeA);
        listRpcConnection.add(rpcConnectionNodeB);
        listRpcConnection.add(rpcConnectionNodeC);
        listRpcConnection.add(rpcConnectionNodeNotary);
        listRpcConnection.add(rpcConnectionNodeObserver);

        listRpcConnection.forEach(rpcConnection -> {
            Optional<CordaRPCConnection> rpcConnectionOpt = Optional.ofNullable(rpcConnection.getRpcConnection());
            if(rpcConnectionOpt.isPresent() && rpcConnectionOpt.get().getProxy() != null) {
                mapTmp.put(rpcConnection.getNodeName(), rpcConnectionOpt.get().getProxy());
            }
        });

        return mapTmp;
    }

    private CordaRPCOps getProxyByNodeName(String nodeName) {
        NodeNameEnum nodeNameEnum = NodeNameEnum.getEnumByNodeName(nodeName);
        return getMapProxy().get(nodeNameEnum);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/info", produces = TEXT_PLAIN_VALUE)
    private String info(String nodeName) {
        Optional<Party> me = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals(nodeName))
                .findFirst();

        return "Node Name: " + me.get().getName().getOrganisation() +
                "  -  City: " + me.get().getName().getLocality() +
                "  -  Country: " + me.get().getName().getCountry();
        //return me.get().getName().toString();
    }

    private boolean getStatus(String nodeName) {
        try {
            getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .filter(party -> party.getName().getOrganisation().equals(nodeName))
                    .findFirst();
            return true;
        } catch(Exception exc) {
            logger.error("getStatus of node %s error %s : ", nodeName, exc.getMessage());
        }

        return false;

    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/allNodesInfo", produces = APPLICATION_JSON_VALUE)
    private List<NodeInfoDTO> allNodesInfo() {
        return getProxyByNodeName("Observer").networkMapSnapshot().stream()
                .map(nodeInfo -> {
                    return new NodeInfoDTO(nodeInfo.getLegalIdentities().get(0),
                            getStatus(nodeInfo.getLegalIdentities().get(0).getName().getOrganisation()));
                })
                .sorted(Comparator.comparing(nodeInfoDTO -> nodeInfoDTO.getName()))
                .collect(Collectors.toList());
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/subscribe", produces = TEXT_PLAIN_VALUE)
    private void subscribe() {
        getMapProxy().forEach((nodeNameEnum, cordaRPCOps) -> {

            // search for party
            Optional<Party> me = getProxyByNodeName(nodeNameEnum.getNodeName()).networkMapSnapshot().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .filter(party -> party.getName().getOrganisation().equals(nodeNameEnum.getNodeName()))
                    .findFirst();

            Observable<Vault.Update<TradeState>> obs = cordaRPCOps
                    .vaultTrack(TradeState.class).getUpdates();

            obs.forEach(elem -> {
                TradeState tradeStateProduced =
                        (new ArrayList<>((Set<StateAndRef<TradeState>>) elem.getProduced()))
                                .get(0).getState().getData();

                updateState(tradeStateProduced, me.get());
            });
        })
        ;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/unsubscribe", produces = TEXT_PLAIN_VALUE)
    private void unsubscribe() {
        getMapProxy().forEach((nodeName, cordaRPCOps) -> {
            Subscription sub = cordaRPCOps.vaultTrack(TradeState.class).getUpdates().subscribe();
            sub.unsubscribe();
        });
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime(String nodeName) {

        return (LocalDateTime.ofInstant(getProxyByNodeName(nodeName).currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/getNodeStatus", produces = TEXT_PLAIN_VALUE)
    private Boolean isNodeAlive(String nodeName) {
        return getProxyByNodeName(nodeName).nodeInfo() != null;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses(String nodeName) {
        return getProxyByNodeName(nodeName).nodeInfo().getAddresses().toString();
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities(String nodeName) {
        return getProxyByNodeName(nodeName).nodeInfo().getLegalIdentities().toString();
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion(String nodeName) {
        return Integer.toString(getProxyByNodeName(nodeName).nodeInfo().getPlatformVersion());
    }

    @CrossOrigin(origins = "*")
 	@GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers(String nodeName) {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = getProxyByNodeName(nodeName).networkMapSnapshot().stream();
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @CrossOrigin(origins = "*")
 	@GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries(String nodeName) {
        return getProxyByNodeName(nodeName).notaryIdentities().toString();
    }

    @CrossOrigin(origins = "*")
 	@GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows(String nodeName) {
        return getProxyByNodeName(nodeName).registeredFlows().toString();
    }

    @CrossOrigin(origins = "*")
 	@GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states(String nodeName) {
        return getProxyByNodeName(nodeName).vaultQuery(ContractState.class).getStates().toString();
    }

    @CrossOrigin(origins = "*")
 	@GetMapping(value = "/statesDetail", produces = MediaType.APPLICATION_JSON_VALUE)
    private List<CdmStateDTO> statesDetail(String nodeName) {

        // search for party
        Optional<Party> me = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals(nodeName))
                .findFirst();

        List<StateAndRef<ContractState>> states= getProxyByNodeName(nodeName).vaultQuery(ContractState.class).getStates();
        return states.stream().map(state -> {
            TradeState tradeState = (TradeState) state.getState().getData();
            return new CdmStateDTO(tradeState, me.get());
            })
                .sorted(Comparator.comparing(tradeState -> ((CdmStateDTO) tradeState).getTimestamp()).reversed())
                .collect(Collectors.toList());
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/statesHistory", produces = MediaType.APPLICATION_JSON_VALUE)
    private List<CdmStateDTO> statesHistory(String nodeName) {

        // search for party
        Optional<Party> me = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals(nodeName))
                .findFirst();

        QueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.ALL);
        return getProxyByNodeName(nodeName).vaultTrackByCriteria(TradeState.class, inputCriteria)
                .getSnapshot().getStates().stream().map(stateRef -> {
                    return new CdmStateDTO(stateRef.getState().getData(), me.get());
                })
                .sorted(Comparator.comparing(tradeState -> ((CdmStateDTO) tradeState).getTimestamp()).reversed())

                .collect(Collectors.toList());

    }


    @CrossOrigin(origins = "*")
    @PostMapping(value="/validateCDMJson")
    private ValidationJsonDTO validateCDMJson(
            @RequestBody String cdmBase64
    ) throws ParseException {
        logger.info("Started request validateCDMJson");
        byte[] decodedBytes = Base64.getMimeDecoder().decode(cdmBase64.replace("\n", "").getBytes());
        String decodedString = new String(decodedBytes);

        try {
            Map<String, Object> result = ParsingUtils.mapValuesFromIrsJSON(decodedString);

            logger.info("Finished request validateCDMJson");

            return new ValidationJsonDTO(true,
                    "CDM Json is validate",
                    (String) result.get("proposee"));
        } catch(Exception exc) {
            logger.error(exc.getMessage());
            return new ValidationJsonDTO(false,
                    "CDM Json is not validate");
        }
    }


    @CrossOrigin(origins = "*")
    @PostMapping(value="/startFlowProposal")
    private String startFlowProposal(
            String nodeName,
            @RequestBody String cdmBase64
        ) throws ParseException {
        logger.info("Started request startFlowProposal");
        byte[] decodedBytes = Base64.getMimeDecoder().decode(cdmBase64.replace("\n", "").getBytes());
        String decodedString = new String(decodedBytes);

        Map<String, Object> mapValuesFromJSON = ParsingUtils.mapValuesFromIrsJSON(decodedString);

        String proposee = (String) mapValuesFromJSON.get("proposee");
        String instrumentType = (String) mapValuesFromJSON.get("instrumentType");
        String instrument = (String) mapValuesFromJSON.get("instrument");
        String quantity = (String) mapValuesFromJSON.get("quantity");
        Double price = (Double) mapValuesFromJSON.get("price");
        String currency = (String) mapValuesFromJSON.get("currency");
        String market = (String) mapValuesFromJSON.get("market");
        String contractualDefinition = (String) mapValuesFromJSON.get("contractualDefinition");
        String masterAgreement = (String) mapValuesFromJSON.get("masterAgreement");

        // search for party
        Optional<Party> proposeeOptional = getProxyByNodeName(proposee).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals(proposee))
                .findFirst();

        // search for observer
        Optional<Party> observerOptional = getProxyByNodeName(NodeNameEnum.OBSERVER.getNodeName()).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals("Observer"))
                .findFirst();

        // if exist then start flow
        if(proposeeOptional.isPresent() && observerOptional.isPresent()) {
            getProxyByNodeName(nodeName).startFlowDynamic(ProposalFlow.Initiator.class,
                    proposeeOptional.get(), observerOptional.get(),
                    instrumentType, instrument, quantity,
                    price, currency, market,
                    contractualDefinition, masterAgreement,
                    cdmBase64);
        } else {
            logger.error("Not found proposee or observer party");
            return "Not found proposee or observer party";
        }

        logger.info("Finished request startFlowProposal");
        return "Finished with success";
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value="/startFlowModificationProposal")
    private String startFlowModificationProposal (
            String nodeName,
            String quantity,
            Double price,
            String proposalId
    ) throws Exception {
        logger.info("Started request startFlowModificationProposal");

        // search for observer
        Optional<Party> observerOptional = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals("Observer"))
                .findFirst();

        // generate UUID
        UniqueIdentifier uniqueIdentifier = UniqueIdentifier.Companion.fromString(proposalId);

        // if exist then start flow
        if(observerOptional.isPresent()) {
            getProxyByNodeName(nodeName).startFlowDynamic(ModificationFlow.Initiator.class,
                    uniqueIdentifier, price, quantity, observerOptional.get())
                    .getReturnValue().get();

        } else {
            logger.error("Not found Observer party");
            return "Not found observer party";
        }

        logger.info("Finished request startFlowModificationProposal");
        return "Finished with success";
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value="/startFlowAcceptProposal")
    private String startFlowAcceptProposal (
            String nodeName, String proposalId
    ) throws Exception {
        logger.info("Started request startFlowAcceptProposal");

        // search for observer
        Optional<Party> observerOptional = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals("Observer"))
                .findFirst();

        // generate UUID
        UniqueIdentifier uniqueIdentifier = UniqueIdentifier.Companion.fromString(proposalId);

        // if exist then start flow
        if(observerOptional.isPresent()) {
            getProxyByNodeName(nodeName).startFlowDynamic(AcceptanceFlow.Initiator.class,
                    uniqueIdentifier, observerOptional.get()).getReturnValue().get();

        } else {
            logger.error("Not found Observer party");
            return "Not found observer party";
        }

        logger.info("Finished request startFlowAcceptProposal");
        return "Finished with success";
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value="/startFlowRejectProposal")
    private String startFlowRejectProposal(
            String nodeName, String proposalId
    ) {
        logger.info("Started request startFlowRejectProposal");

        // search for observer
        Optional<Party> observerOptional = getProxyByNodeName(nodeName).networkMapSnapshot().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .filter(party -> party.getName().getOrganisation().equals("Observer"))
                .findFirst();

        // generate UUID
        UniqueIdentifier uniqueIdentifier = UniqueIdentifier.Companion.fromString(proposalId);

        // if exist then start flow
        if(observerOptional.isPresent()) {
            getProxyByNodeName(nodeName).startFlowDynamic(RejectFlow.Initiator.class,
                    uniqueIdentifier, observerOptional.get());
        } else {
            logger.error("Not found Observer party");
            return "Not found observer party";
        }

        logger.info("Finished request startFlowRejectProposal");
        return "Finished with success";
    }

}
