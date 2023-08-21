package com.flansmod.common.types;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Definitions<TDefinitionType extends JsonDefinition> extends SimpleJsonResourceReloadListener
{
	public static GsonBuilder createSerializer()
	{
		return new GsonBuilder();
	}

	private static final ResourceLocation EMPTY = new ResourceLocation("empty");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = createSerializer().create();
	private Map<ResourceLocation, TDefinitionType> tables = ImmutableMap.of();
	private Map<Integer, TDefinitionType> hashmap = new HashMap();
	private final TDefinitionType INVALID;
	public interface Factory<TDefinitionType  extends JsonDefinition>
	{
		TDefinitionType Create(ResourceLocation key);
	}
	private final Factory<TDefinitionType> createFunc;

	public Definitions(String folderName,
					   Class<? extends TDefinitionType> clazz,
					   TDefinitionType invalid,
					   Factory<TDefinitionType> createFunctor)
	{
		super(GSON, folderName.toLowerCase());
		createFunc = createFunctor;
		INVALID = invalid;
		DefinitionParser.IterativelyCreateParsers(clazz);
	}

	public TDefinitionType Get(ResourceLocation location)
	{
		return tables.getOrDefault(location, INVALID);
	}

	public TDefinitionType ByHash(int hash)
	{
		return hashmap.get(hash);
	}

	public void RunOnMatch(String key, Consumer<TDefinitionType> resultFunction)
	{
		for(var kvp : tables.entrySet())
		{
			if(kvp.getKey().getPath().equals(key))
				resultFunction.accept(kvp.getValue());
		}
	}
	public void RunOnMatches(Function<TDefinitionType, Boolean> matchFunction, Consumer<TDefinitionType> resultFunction)
	{
		for(var kvp : tables.entrySet())
		{
			if(matchFunction.apply(kvp.getValue()))
				resultFunction.accept(kvp.getValue());
		}
	}
	public List<TDefinitionType> Find(Function<TDefinitionType, Boolean> matchFunction)
	{
		List<TDefinitionType> defs = new ArrayList<>();
		for(var kvp : tables.entrySet())
		{
			if(matchFunction.apply(kvp.getValue()))
				defs.add(kvp.getValue());
		}
		return defs;
	}

	protected void apply(Map<ResourceLocation, JsonElement> sources, ResourceManager resourceManager, ProfilerFiller p_79216_)
	{
		ImmutableMap.Builder<ResourceLocation, TDefinitionType> builder = ImmutableMap.builder();
		JsonElement jsonelement = sources.remove(EMPTY);
		if (jsonelement != null)
		{
			LOGGER.warn("Datapack tried to redefine {} definition, ignoring", (Object)EMPTY);
		}

		sources.forEach((key, value) ->
		{
			try
			{
				Resource res = resourceManager.getResource(getPreparedPath(key)).orElse(null);
				TDefinitionType def = createFunc.Create(key);
				DefinitionParser.LoadFromJSON(def, value);
				builder.put(key, def);
			}
			catch (Exception exception)
			{
				LOGGER.error("Couldn't parse gun definition {}", value, exception);
			}

		});
		builder.put(EMPTY, INVALID);
		ImmutableMap<ResourceLocation, TDefinitionType> immutablemap = builder.build();
		this.tables = immutablemap;
		for(var kvp : tables.entrySet())
		{
			hashmap.put(kvp.getKey().hashCode(), kvp.getValue());
		}
	}

	public JsonElement serialize(TDefinitionType def)
	{
		return GSON.toJsonTree(def);
	}

	public Set<ResourceLocation> getIds()
	{
		return this.tables.keySet();
	}
}
