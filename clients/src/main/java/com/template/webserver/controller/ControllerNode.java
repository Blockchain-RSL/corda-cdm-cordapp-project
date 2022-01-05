package com.template.webserver.controller;

import com.google.gson.Gson;
import com.template.flows.AcceptanceFlow;
import com.template.flows.ProposalFlow;
import com.template.flows.RejectFlow;
import com.template.states.TradeState;
import com.template.webserver.dto.CdmStateDTO;
import com.template.webserver.dto.TextMessageDTO;
import com.template.webserver.enums.NodeNameEnum;
import com.template.webserver.rpc.impl.*;
import com.template.webserver.utils.ParsingUtils;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
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
        //msg.setMessage("Ciao");
        template.convertAndSend("/topic/message", msg);
    }

    /******************* WEBSOCKET END *****************/

    private final CordaRPCOps proxyNodeA;
    private final CordaRPCOps proxyNodeB;
    private final CordaRPCOps proxyNodeC;
    private final CordaRPCOps proxyNotary;
    private final CordaRPCOps proxyObserver;

    public ControllerNode(
            NodeRPCConnectionNodeA rpcNodeA,
            NodeRPCConnectionNodeB rpcNodeB,
            NodeRPCConnectionNodeC rpcNodeC,
            NodeRPCConnectionNodeNotary rpcNotary,
            NodeRPCConnectionNodeObserver rpcObserver) {

        proxyNodeA = rpcNodeA.proxy;
        proxyNodeB = rpcNodeB.proxy;
        proxyNodeC = rpcNodeC.proxy;
        proxyNotary = rpcNotary.proxy;
        proxyObserver = rpcObserver.proxy;

        subscribe();
    }

    private Map<NodeNameEnum, CordaRPCOps> getMapProxy() {
        Map<NodeNameEnum, CordaRPCOps> mapTmp = new HashMap<>();
        mapTmp.put(NodeNameEnum.PARTY_A, proxyNodeA);
        mapTmp.put(NodeNameEnum.PARTY_B, proxyNodeB);
        mapTmp.put(NodeNameEnum.PARTY_C, proxyNodeC);
        mapTmp.put(NodeNameEnum.NOTARY, proxyNotary);
        mapTmp.put(NodeNameEnum.OBSERVER, proxyObserver);
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

        return me.get().getName().toString();
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/subscribe", produces = TEXT_PLAIN_VALUE)
    private void subscribe() {
        getMapProxy().forEach((nodeNameEnum, cordaRPCOps) -> {
            System.out.println("NodeEnum " + nodeNameEnum.getNodeName());

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
        }).collect(Collectors.toList());
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value="/startFlowProposal")
    private String startFlowProposal(
            String nodeName,
            @RequestBody String cdmBase64) throws ParseException {
        logger.info("Started request startFlowProposal");
        byte[] decodedBytes = Base64.getMimeDecoder().decode(cdmBase64.replace("\n", "").getBytes());
        String decodedString = new String(decodedBytes);

        Map<String, Object> mapValuesFromJSON = ParsingUtils.mapValuesFromJSON(decodedString);
        mapValuesFromJSON.forEach((key, value) -> {
            logger.info("Found key %s and values %s,",
                    key, value);
        });

        String proposee = (String) mapValuesFromJSON.get("proposee");
        String instrumentType = (String) mapValuesFromJSON.get("instrumentType");
        String instrument = (String) mapValuesFromJSON.get("instrument");
        String quantity = (String) mapValuesFromJSON.get("quantity");
        Double price = (Double) mapValuesFromJSON.get("price");
        String currency = (String) mapValuesFromJSON.get("currency");
        String market = (String) mapValuesFromJSON.get("market");

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
                    price, currency, market);
        } else {
            logger.error("Not found proposee or observer party");
            return "Not found proposee or observer party";
        }

        logger.info("Finished request startFlowProposal");
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
