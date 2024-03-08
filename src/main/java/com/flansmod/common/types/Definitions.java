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

import javax.annotation.Nonnull;
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
	private Map<ResourceLocation, TDefinitionType> keyedDefinitions = ImmutableMap.of();
	private Map<ResourceLocation, TDefinitionType> prefixKeyedDefinitions = ImmutableMap.of();
	private final Map<Integer, TDefinitionType> hashKeyedDefinitions = new HashMap<>();
	private final TDefinitionType INVALID;
	private final String FolderName;
	public interface Factory<TDefinitionType  extends JsonDefinition>
	{
		TDefinitionType Create(@Nonnull ResourceLocation key);
	}
	private final Factory<TDefinitionType> createFunc;

	public Definitions(@Nonnull String folderName,
					   @Nonnull Class<? extends TDefinitionType> clazz,
					   @Nonnull TDefinitionType invalid,
					   @Nonnull Factory<TDefinitionType> createFunctor)
	{
		super(GSON, folderName.toLowerCase());
		createFunc = createFunctor;
		FolderName = folderName;
		INVALID = invalid;
		DefinitionParser.IterativelyCreateParsers(clazz);
	}

	@Nonnull
	public TDefinitionType Get(@Nonnull ResourceLocation location)
	{
		TDefinitionType def = keyedDefinitions.get(location);
		if(def != null)
			return def;
		return prefixKeyedDefinitions.getOrDefault(location, INVALID);
	}

	@Nonnull
	public TDefinitionType ByHash(int hash)
	{
		return hashKeyedDefinitions.getOrDefault(hash, INVALID);
	}

	public void RunOnMatch(@Nonnull String key, @Nonnull Consumer<TDefinitionType> resultFunction)
	{
		for(var kvp : keyedDefinitions.entrySet())
		{
			if(kvp.getKey().getPath().equals(key))
				resultFunction.accept(kvp.getValue());
		}
	}
	public void RunOnMatches(@Nonnull Function<TDefinitionType, Boolean> matchFunction, @Nonnull Consumer<TDefinitionType> resultFunction)
	{
		for(var kvp : keyedDefinitions.entrySet())
		{
			if(matchFunction.apply(kvp.getValue()))
				resultFunction.accept(kvp.getValue());
		}
	}
	@Nonnull
	public List<TDefinitionType> Find(@Nonnull Function<TDefinitionType, Boolean> matchFunction)
	{
		List<TDefinitionType> defs = new ArrayList<>();
		for(var kvp : keyedDefinitions.entrySet())
		{
			if(matchFunction.apply(kvp.getValue()))
				defs.add(kvp.getValue());
		}
		return defs;
	}

	protected void apply(@Nonnull Map<ResourceLocation, JsonElement> sources, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller p_79216_)
	{
		ImmutableMap.Builder<ResourceLocation, TDefinitionType> builder = ImmutableMap.builder();
		ImmutableMap.Builder<ResourceLocation, TDefinitionType> builderWithPrefix = ImmutableMap.builder();
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
				builderWithPrefix.put(key.withPrefix(FolderName+"/"), def);
			}
			catch (Exception exception)
			{
				LOGGER.error("Couldn't parse gun definition {}", value, exception);
			}

		});
		builder.put(EMPTY, INVALID);
		builderWithPrefix.put(EMPTY.withPrefix(FolderName+"/"), INVALID);
		keyedDefinitions = builder.build();
		prefixKeyedDefinitions = builderWithPrefix.build();

		for(var kvp : keyedDefinitions.entrySet())
			hashKeyedDefinitions.put(kvp.getKey().hashCode(), kvp.getValue());
		for(var kvp : prefixKeyedDefinitions.entrySet())
			hashKeyedDefinitions.put(kvp.getKey().hashCode(), kvp.getValue());
	}
	@Nonnull
	public JsonElement serialize(@Nonnull TDefinitionType def)
	{
		return GSON.toJsonTree(def);
	}
	@Nonnull
	public Set<ResourceLocation> getIds()
	{
		return this.keyedDefinitions.keySet();
	}
}
