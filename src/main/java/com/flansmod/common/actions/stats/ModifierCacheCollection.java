package com.flansmod.common.actions.stats;

public class ModifierCacheCollection
{
	/*
	// Keyed per actionGroupPath
	private static final String MATCH_ANY_GROUP_PATH = "";
	private final Map<String, ModifierCache> ModifierCaches;
	private final Supplier<Integer> HashFunction;
	private final Consumer<ModifierCache> CacheBuilder;

	public ModifierCacheCollection(@Nonnull Supplier<Integer> hashFunction,
								   @Nonnull Consumer<ModifierCache> cacheBuilder)
	{
		ModifierCaches = new HashMap<>();
		HashFunction = hashFunction;
		CacheBuilder = cacheBuilder;
	}

	@Nonnull
	private ModifierCache GetModifierCache(@Nonnull String actionGroupPath)
	{
		int currentModifierSourceHash = HashFunction.get();

		ModifierCache existingCache = ModifierCaches.get(actionGroupPath);
		if(existingCache == null || existingCache.ModifierHash() != currentModifierSourceHash)
		{
			if(existingCache == null)
			{
				existingCache = new ModifierCache();
				ModifierCaches.put(actionGroupPath, existingCache);
			}

			existingCache.Clear();
			CacheBuilder.accept(existingCache);
			existingCache.CompleteCaching(currentModifierSourceHash);
		}
		return existingCache;
	}

	@Nonnull
	public Function<String, FloatModifier> GetFloatModifierLookupForActionGroup(@Nonnull String actionGroupPath)
	{
		return GetModifierCache(actionGroupPath).GetFloatModifierLookup();
	}
	@Nonnull
	public Function<String, String> GetStringModificationLookupForActionGroup(@Nonnull String actionGroupPath)
	{
		return GetModifierCache(actionGroupPath).GetStringModificationLookup();
	}

	@Nonnull
	public FloatModifier GetFloatModifier(@Nonnull String stat) { return GetFloatModifier(stat, MATCH_ANY_GROUP_PATH); }
	@Nonnull
	public FloatModifier GetFloatModifier(@Nonnull String stat, @Nonnull String actionGroupPath)
	{
		return GetFloatModifierLookupForActionGroup(actionGroupPath).apply(stat);
	}
	@Nullable
	public String GetModifiedString(@Nonnull String stat) { return GetModifiedString(stat, MATCH_ANY_GROUP_PATH); }
	@Nullable
	public String GetModifiedString(@Nonnull String stat, @Nonnull String actionGroupPath)
	{
		return GetStringModificationLookupForActionGroup(actionGroupPath).apply(stat);
	}

	 */
}
