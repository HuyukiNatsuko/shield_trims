package com.HuyukiNatsuko.shieldtrims; 
import net.fabricmc.api.ClientModInitializer; 
import net.minecraft.client.renderer.special.SpecialModelRenderers; 
import net.minecraft.resources.Identifier;
public class ShieldTrimsClient
implements ClientModInitializer {
    @Override public void onInitializeClient() 
        {SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath("shield_trims", "trimmed_shield"),TrimmedShieldRenderer.Unbaked.MAP_CODEC);}}