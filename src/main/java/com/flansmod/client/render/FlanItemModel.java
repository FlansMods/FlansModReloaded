package com.flansmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FlanItemModel extends BakedModelWrapper<BakedModel>
{




    protected String modName;
    protected String modelName;
    protected Map<String, BakedModel> parts = new HashMap<>();
    protected Map<String, ResourceLocation> textures = new HashMap<>();

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
        {
            BakedModel part = lookupPart(models, name);
            if (part != null)
            {
                parts.put(name, part);

                List<BakedQuad> quads = part.getQuads(null, null, rnd, null, null);
                if (quads.size() > 0)
                {
                    ResourceLocation texLoc = quads.get(0).getSprite().atlasLocation();
                    textures.put(name, new ResourceLocation(texLoc.getNamespace(), "textures/" + texLoc.getPath() + ".png"));
                }
            }
        }
    }

    protected BakedModel lookupPart(Map<ResourceLocation, BakedModel> models, String name)
    {
        ResourceLocation partModel = getPartModelLocation(name);
        ModelResourceLocation inventoryPartModel = new ModelResourceLocation(partModel, "inventory");
        if(models.containsKey(inventoryPartModel))
            return models.get(inventoryPartModel);
        return null;
    }

    // -- Rendering. Grab the pieces you need --
    public final BakedModel getOriginalModel()
    {
        return originalModel;
    }

    public BakedModel getModelPart(String name)
    {
        if(name.equals("body"))
            return originalModel;

        return parts.get(name);
    }

    public final List<ResourceLocation> getModelLocations()
    {
        List<ResourceLocation> locations = new ArrayList<>(parts.size() + 1);
        for(String partName : parts.keySet())
            locations.add(getPartModelLocation(partName));
        //locations.add(new ResourceLocation(modName, modelName));
        return locations;
    }

    public ResourceLocation GetTextureForPart(String partName)
    {
        if(textures.containsKey(partName))
        {
            return textures.get(partName);
        }
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }

    private static RandomSource rnd = RandomSource.create();
    public final Collection<ResourceLocation> GetTextureLocations()
    {
        return textures.values();

        /*
        List<ResourceLocation> locations = new ArrayList<>(1);
        for(var kvp : parts.entrySet())
        {
            ModelData modelData = ModelData.EMPTY;
            if(kvp.getValue() != null)
            {
                for (var quad : kvp.getValue().getQuads(null, null, rnd, null, null))
                {
                    if (!locations.contains(quad.getSprite().atlasLocation()))
                        locations.add(quad.getSprite().atlasLocation());
                }
            }
        }

        for(int i = 0; i < locations.size(); i++)
        {
            String namespace = locations.get(i).getNamespace();
            String path = locations.get(i).getPath();

            locations.set(i, new ResourceLocation(namespace, "textures/" + path + ".png"));
        }

        return locations;*/
    }

    protected ResourceLocation getPartModelLocation(String name)
    {
        if(name.equals("body"))
            return new ResourceLocation(modName, modelName);
        return new ResourceLocation(modName, modelName + "/" + name);
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
