package com.template.webserver.rpc;

import com.github.rholder.retry.*;
import com.google.common.base.Predicate;
import com.template.webserver.enums.NodeNameEnum;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.client.rpc.GracefulReconnect;
import net.corda.client.rpc.RPCException;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

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

    public void initialiseNodeRPCConnection() throws ExecutionException, RetryException {

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

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(new Predicate<Boolean>() {
                    @Override
                    public boolean apply(@Nullable Boolean aBoolean) {
                        try {
                            rpcConnection = rpcClient.start(getUser(), getPsw(), gracefulReconnect);
                            aBoolean = false;
                            return false;
                        } catch(Exception  exc) {
                            aBoolean = true;
                            return true;
                        }
                    }
                })
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
