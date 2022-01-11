package com.template.webserver.rpc.impl;

import com.template.webserver.enums.NodeNameEnum;
import com.template.webserver.rpc.NodeRPCConnectionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class NodeRPCConnectionNodeB extends NodeRPCConnectionNode implements AutoCloseable {

    // The host of the node we are connecting to.
    @Value("${nodeB.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${nodeB.rpc.auth.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${nodeB.rpc.auth.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${nodeB.rpc.port}")
    private int rpcPort;

    private static final Logger logger = LoggerFactory.getLogger(NodeRPCConnectionNodeB.class);

    private static final NodeNameEnum NODE_NAME = NodeNameEnum.PARTY_B;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        try {
            super.initialiseNodeRPCConnection();
        } catch(Exception exc) {
            logger.error("initialiseNodeRPCConnection", exc.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        getRpcConnection().notifyServerAndClose();
    }

    @Override
    public NodeNameEnum getNodeName() {
        return NODE_NAME;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return rpcPort;
    }

    @Override
    public String getUser() {
        return username;
    }

    @Override
    public String getPsw() {
        return password;
    }


}
