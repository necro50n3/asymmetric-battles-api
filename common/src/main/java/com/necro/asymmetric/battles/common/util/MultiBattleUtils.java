package com.necro.asymmetric.battles.common.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MultiBattleUtils {
    private static final Set<UUID> ACTOR_REQUEST_QUEUE = new HashSet<>();

    public static void add(UUID actorID) {
        ACTOR_REQUEST_QUEUE.add(actorID);
    }

    public static boolean has(UUID actorID) {
        return ACTOR_REQUEST_QUEUE.remove(actorID);
    }
}
