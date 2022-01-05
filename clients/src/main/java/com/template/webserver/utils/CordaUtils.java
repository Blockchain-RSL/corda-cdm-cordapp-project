package com.template.webserver.utils;

import net.corda.core.node.NodeInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

/**
 *  Corda utils to get all utility about Corda
 */
public class CordaUtils {

    private CordaUtils() {
        //Hide constructor for utils
    }

    /** Helpers for filtering the network map cache. */
    public static String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private static boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

}
