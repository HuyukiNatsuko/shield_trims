package com.HuyukiNatsuko.shieldtrims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;

import net.minecraft.client.model.geom.ModelLayers;

import net.minecraft.client.model.object.equipment.ShieldModel;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;

import net.minecraft.client.resources.palette.Palette;
import net.minecraft.client.resources.palette.PalettedTextureManager;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;

import net.minecraft.network.chat.TextColor;

import net.minecraft.resources.Identifier;

import net.minecraft.util.Unit;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import org.joml.Vector3fc;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class TrimmedShieldRenderer implements SpecialModelRenderer<DataComponentMap> {
    private static final Identifier TRIM_BASE = Identifier.withDefaultNamespace("trim_base");
    private final ShieldSpecialRenderer vanilla;
    private final ShieldModel model;
    private final Map<Identifier, Boolean> paletteExists = new HashMap<>();
    public TrimmedShieldRenderer(ShieldSpecialRenderer vanilla, ShieldModel model) {this.vanilla = vanilla; this.model = model;}

    @Override public DataComponentMap extractArgument(ItemStack stack) {return stack.immutableComponents();}

    @Override
    public void submit(@Nullable DataComponentMap components, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
        vanilla.submit(components, poseStack, collector, light, overlay, hasFoil, outlineColor);
        if (components == null) return; ArmorTrim trim = components.get(DataComponents.TRIM);
        if (trim == null) return; TrimPattern pattern = trim.pattern().value(); Identifier asset = pattern.assetId();
        if (!asset.getNamespace().equals("minecraft")) return; TrimMaterial material = trim.material().value(); Identifier palette = material.paletteId();int tint = -1;
        if (!hasPalette(palette)) {palette = TRIM_BASE; tint = tintFrom(material);}
        Identifier baseTexture = Identifier.fromNamespaceAndPath("shield_trims", "trims/entity/shield/" + asset.getPath());
        PalettedTextureManager.Handle texture = Minecraft.getInstance().getPalettedTextureManager() .getOrPrepare(baseTexture, palette);

        @SuppressWarnings("null")
        int bannerLayers = components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size();
        collector.order(bannerLayers + 2).submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.armorTrim(texture.textureLocation(), pattern.decal()), light, overlay, tint, texture, outlineColor);
    }

    private boolean hasPalette(Identifier palette) {return paletteExists.computeIfAbsent(palette, _ -> Minecraft.getInstance().getResourceManager().getResource(Palette.ID_CONVERTER.idToFile(palette)).isPresent());}

    private static final float BRIGHTNESS = 2f;

    private static int tintFrom(TrimMaterial material) {
        TextColor color = material.description().getStyle().getColor();
        if (color == null) return -1;
        int rgb = color.getValue(), r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF, max = Math.max(r, Math.max(g, b)); float scale = BRIGHTNESS * 255f / 224f;
        if (max > 0) scale = Math.min(scale, 255f / max); return 0xFF000000 | Math.round(r * scale) << 16 | Math.round(g * scale) << 8 | Math.round(b * scale);
    }

    @Override public void getExtents(Consumer<Vector3fc> output) {vanilla.getExtents(output);}

    public record Unbaked() implements SpecialModelRenderer.Unbaked<DataComponentMap> {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override public SpecialModelRenderer<DataComponentMap> bake(SpecialModelRenderer.BakingContext context) {
            ShieldModel model = new ShieldModel(context.entityModelSet().bakeLayer(ModelLayers.SHIELD));
            return new TrimmedShieldRenderer(new ShieldSpecialRenderer(context.sprites(), model), model);
        }
        @Override public MapCodec<Unbaked> type() {return MAP_CODEC;}
    }
}