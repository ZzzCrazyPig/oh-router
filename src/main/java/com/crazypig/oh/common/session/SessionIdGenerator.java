package com.crazypig.oh.common.session;

import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

import java.util.UUID;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public class SessionIdGenerator {

    private static IdGenerator idGenerator = new JdkIdGenerator();

    public static String generate() {
        UUID uuid = idGenerator.generateId();
        return uuid.toString().replaceAll("-", "");
    }

}
