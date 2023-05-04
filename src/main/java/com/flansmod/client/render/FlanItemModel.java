package com.flansmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class FlanItemModel extends BakedModelWrapper<BakedModel>
{
    protected String modName;
    protected String modelName;
    protected Map<String, BakedModel> parts = new HashMap<>();

    // -- Construction. Register the names of parts you want to split out --
    public FlanItemModel(BakedModel template, String modName, String modelName)
    {
        super(template);
        this.modName = modName;
        this.modelName = modelName;
    }

    protected void addParts(String... partNames)
    {
        for (String name : partNames)
            this.parts.put(name, null);
    }

    // -- Baking. Call these to bake the parts individually --
    public void bakeParts(ModelEvent.ModifyBakingResult event)
    {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (String name : parts.keySet())
            parts.put(name, lookupPart(models, name));
    }

    protected BakedModel lookupPart(Map<ResourceLocation, BakedModel> models, String name)
    {
        return models.get(getPartModelLocation(name));
    }

    // -- Rendering. Grab the pieces you need --
    public final BakedModel getOriginalModel()
    {
        return originalModel;
    }

    public BakedModel getModelPart(String name)
    {
        return parts.get(name);
    }

    public final List<ResourceLocation> getModelLocations()
    {
        return parts.keySet().stream().map(this::getPartModelLocation).collect(Collectors.toList());
    }

    protected ResourceLocation getPartModelLocation(String name)
    {
        return new ResourceLocation(modName, "item/" + modelName + "/" + name);
    }

    @Override
    public boolean isCustomRenderer()
    {
        return true;
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack mat, boolean leftHand)
    {
        super.applyTransform(cameraTransformType, mat, leftHand);
        return this;
    }
}
