package com.dotashowcase.inventoryservice.service.lock;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// per inventory, single app instance only
@Component
public class InventorySyncLock {

    private final Set<Long> lockedSteamIds = ConcurrentHashMap.newKeySet();

    public boolean tryLock(Long steamId) {
        return lockedSteamIds.add(steamId);
    }

    public void unlock(Long steamId) {
        lockedSteamIds.remove(steamId);
    }
}
