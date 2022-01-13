package com.template.webserver.rpc;

import com.github.rholder.retry.*;
import com.template.webserver.controller.ControllerNode;
import com.template.webserver.enums.NodeNameEnum;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.client.rpc.GracefulReconnect;
import net.corda.client.rpc.RPCException;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 *
 */
public abstract class NodeRPCConnectionNode {

    // abstract methods
    public abstract NodeNameEnum getNodeName();
    public abstract String getHost();
    public abstract Integer getPort();
    public abstract String getUser();
    public abstract String getPsw();

    // concrete
    // logger
    private static final Logger logger = LoggerFactory.getLogger(NodeRPCConnectionNode.class);

    // components
    private CordaRPCConnection rpcConnection;

    //private ControllerNode controllerNode;

    /** This method handles the reconnection of RPC client to a node, in two cases:
     *  1) Server starts with all node up, and later some node goes down
     *  2) Server starts but not all nodes are runnig
     */
    public void initialiseNodeRPCConnection() {

        // this parameter handles the first case of reconnection to RPC client
        GracefulReconnect gracefulReconnect = new GracefulReconnect(
                () -> {
                    logger.info("on disconnect");
                },
                () -> {
                    logger.info("on reconnect");
                    //controllerNode.subscribeByNodeRPC(getNodeName(), getRpcProxy());
                },
                -1);

        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(getHost(), getPort());
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(getUser(), getPsw(), gracefulReconnect);
    /*
        // This block of code handles the second case of reconnection:
        // If a node is not connected, a pool of threads is created to pooling the node to retry the RPC connection

        // retryer defines the conditions to polling the node: if connection is down the retryer will try to reconnect
        // every 5 seconds and will not stop until it reconnects
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(aBoolean -> {
                        try {
                            rpcConnection = rpcClient.start(getUser(), getPsw(), gracefulReconnect);
                            controllerNode.subscribeByNodeRPC(getNodeName(), getRpcProxy());
                            aBoolean = false;
                        } catch(Exception  exc) {
                            aBoolean = true;
                        }
                        return aBoolean;
                })
                .retryIfExceptionOfType(RPCException.class)
                .withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();

        // retrier retry the connection if the callable have the same result of predicate (aBoolean)
        // if reconnection fails, predicate throws an exception and return true like the callable. Retyer keep trying.
        // If client is reconnected predicate return false, not the same result of callable. Retyer will stop.


        // thread's pool (one thread) creation where the retryer will run
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Void> future =
                new FutureTask<Void>(() -> {
                        try {
                            retryer.call(() -> true);
                        } catch (Exception exc) {
                            logger.error("Error into retrier connection rpc: ", exc.getMessage());
                        }
                        return null;
                });
        executor.execute(future);*/
    }

    public CordaRPCConnection getRpcConnection() {
        return rpcConnection;
    }

    public CordaRPCOps getRpcProxy() {
        return rpcConnection.getProxy();
    }

    /*public void setControllerNode(ControllerNode controllerNode) {
        this.controllerNode = controllerNode;
    }*/
}
