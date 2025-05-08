package com.github.pierrepressure.krunkmode;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        AutoFishManager.INSTANCE.onClientTick(event);
    }
}
