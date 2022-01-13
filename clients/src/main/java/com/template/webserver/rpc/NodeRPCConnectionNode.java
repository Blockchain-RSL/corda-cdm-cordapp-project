package com.template.webserver.rpc;

import com.template.webserver.enums.NodeNameEnum;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.client.rpc.GracefulReconnect;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

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

    /**
     * Initialize rpc client constructor with reconnect management with gracefull and first connection retries with
     * polling execution single Thread
     */
    public void initialiseNodeRPCConnection() {

        GracefulReconnect gracefulReconnect = new GracefulReconnect(
                () -> {
                    logger.info("on disconnect");
                },
                () -> {
                    logger.info("on reconnect");
                },
                -1);

        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(getHost(), getPort());
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(getUser(), getPsw(), gracefulReconnect);

        /*
        // get predicate function for rpc client
        Predicate<Boolean> predicateRetry = aBoolean -> {
            try {
                rpcConnection = rpcClient.start(getUser(), getPsw(), gracefulReconnect);
                aBoolean = false;
            } catch(Exception  exc) {
                aBoolean = true;
            }
            return aBoolean;
        };

        // retryer component.-..

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(predicateRetry)
                .retryIfExceptionOfType(RPCException.class)
                .withWaitStrategy(WaitStrategies.exponentialWait(100, 5, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Void> future =
                new FutureTask<Void>(new Callable<Void>() {
                    public Void call() {
                        try {
                            retryer.call(callable);
                        } catch (Exception exc) {
                            logger.error("Error into retrier connection rpc: ", exc.getMessage());
                        }
                        return null;
                    }
                });
        executor.execute(future);
*/
    }

    Callable<Boolean> callable = new Callable<Boolean>() {
        public Boolean call() throws Exception {
            return true; // do something useful here
        }
    };


    public CordaRPCConnection getRpcConnection() {
        return rpcConnection;
    }

    public CordaRPCOps getRpcProxy() {
        return rpcConnection.getProxy();
    }
}
