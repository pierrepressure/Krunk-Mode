package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.features.FarmMelon;
import com.github.pierrepressure.krunkmode.features.FishManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Only call FishManager if it's enabled
        if (FishManager.INSTANCE.isEnabled()) {
            FishManager.INSTANCE.onTick(event);
        }

        // Only call FarmMelon if it's running
        if (FarmMelon.INSTANCE.isRunning()) {
            FarmMelon.INSTANCE.onTick(event);
        }
    }
}
