package com.template.webserver.rpc.impl;

import com.template.webserver.enums.NodeNameEnum;
import com.template.webserver.rpc.NodeRPCConnectionNode;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 */
@Component
public class NodeRPCConnectionNodeA implements AutoCloseable, NodeRPCConnectionNode {
    // The host of the node we are connecting to.
    @Value("${nodeA.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${nodeA.rpc.auth.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${nodeA.rpc.auth.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${nodeA.rpc.port}")
    private int rpcPort;

    private static final NodeNameEnum NODE_NAME = NodeNameEnum.PARTY_A;

    private CordaRPCConnection rpcConnection;
    public CordaRPCOps proxy;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(username, password);
        proxy = rpcConnection.getProxy();
    }

    @PreDestroy
    public void close() {
        rpcConnection.notifyServerAndClose();
    }

    @Override
    public NodeNameEnum getNodeName() {
        return NODE_NAME;
    }
}
