package com.crazypig.oh.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by chenjianxin on 2021/7/8.
 */
public class HostUtils {

    public static String serverHost() throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        return hostAddress;
    }

}
