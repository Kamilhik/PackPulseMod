package ru.fwx.packpulse;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(PackPulseRuntime.MOD_ID)
public final class PackPulseNeoForge {
    public PackPulseNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PackPulseRuntime.start(FMLPaths.GAMEDIR.get()));
    }
}

