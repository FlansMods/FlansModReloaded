

### 0.4
* Allow NetSync on sequential reload action groups - PTTheta
* Bullet casing ejection - PTTheta
* Fix action stack sync on server, generic delay parameter for all action instances - PTTheta
* Clean up some log spam - Flan
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - PTTheta
* Nasty hack to fix effect renderer - PTTheta
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* Added Modrinth upload tasks - Flan
* Fix Bullet acceleration not applying - PTTheta
* Bump up BulletEntity render distance - PTTheta
* Bit stinky but it seems to work - PTTheta
* Lock-on for multiple shot magazines, fix some guidance crashes - PTTheta
* Bullet trails fix, effect positioning fix - PTTheta
* Fix third person transforms and poseStack -> TransformStack - Flan
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* Consistent physics mod id, ready for build - Flan
* Particle trails for projectiles initial commit - PTTheta
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* 3rd person render tweaks - Flan
* Fix #69 - DUPE FIX - Flan
* Delay function for SpawnParticleAction and AttachEffectAction - PTTheta
* Very basic, may be worth integrating a delay into the base Start > Trigger > Tick setup for ActionInstance - PTTheta
* Spawn Particle Action - PTTheta
* Individual IDs for actions and modifiers - PTTheta
* Gun modification table rework + paint + Patreon - Flan
* Some vender fixes - Flan
* Try to fix vender radio - Flan
* Ignore data run from tinkers branch - Flan
* Physic - Flan
* Better construct and hints - Flan
* Tons of teams features - Flan
* Teamsy things - Flan
* Teams work - Flan
* Fix some bugs - Flan
* Interfaces for collision system - Flan
* Constructs - Flan
* Teams construct work still - Flan
* Some more instancing - Flan
* Some start on huds - Flan
* More teams stuff - Flan
* More gamemode stuff - Flan
* Add files - Flan
* More TeamsAPI and construct - Flan
* Teams doesn't crash on startup - Flan
* Teams stuff including dim swap - Flan
* Some more Teams functionality - Flan
* TeamsMod basic structures - Flan
* TurboReading - Flan
* Allow TurboBox/TurboShapeBox model .json - Flan
* Redo Turbo baking to allow box, shapebox etc definitions - Flan
* Why so bouncy - Flan
* Physics are very slidy but getting better - Flan
* The cube topples - Flan
* Fix just so many bugs in the physic - Flan
* Added bullet bags, not working yet - Flan
* More physics and big memory leak fix - Flan
* New resolver logic - Flan
* Merge pull request #72 from DevArchwave/1.20 - Flan
* Return empty optional when there is no capability for a null ordinal - Flan
* PhysicsMod is fully independent now - Flan
* Move DebugRenderer into Physics mod - Flan
* Cut some old debug in transform - Flan
* More net work - Flan
* More netsync prep - Flan
* Start of physics net sync - Flan
* Add start of some collision tests - Flan
* Move resolve step into tasks - Flan
* More camelCasing - Flan
* CamelCase in Physics library - Flan
* Some more physics - Flan
* Axis-aligned collisions working - Flan
* Separators working - Flan
* Massive collision work, not yet tested - Flan
* Turret Block Works - Flan
* Shooty Block - Flan
* Return empty optional when there is no capability for a null ordinal - DevArchWave
* Remove unneccessary debug. Whoops - PTTheta
* Bullet guidance modes and lock-on logic - PTTheta
* Merge pull request #71 from FlansMods/vehicles - Flan
* Vehicles - Flan
* Physics better but not working - Flan
* Fix a lot of 1st frame bugs with physics - Flan
* A whole day just fixing arrows - Flan
* Do units right - Flan
* Units will make me sane I hope - Flan
* Some stuff - Flan
* Whoops debug timer - Flan
* Fix a lot of weird things with distillers - Flan
* Consume inputs at end of processing - Flan
* Give distillation sided caps - Flan
* Fix mineable settings for blocks - Flan
* Fix part fab being picky with material matches - Flan
* Render the amounts of ingredient (even partial) - Flan
* Allow repeat crafting and make default hoppers better - Flan
* Add pocket workbench - Flan
* Fix the rare grenade crash - Flan
* Fix missing block tags for crafting - Flan
* Fix item transforms - Flan
* Added sus grenades - Flan
* Frog nades - Flan
* Fix #56 - Flan
* More vehicle progress - Flan
* We compile again - Flan
* Gobbledygook - Flan
* Physics - Flan
* Some vehicles, also FIX HOT SWAP :D - Flan
* Stuck in wheel sync, maybe not parts - Flan
* Vehicle syncing - Flan
* Tons of WIP, doesn't compile, just backing up - Flan
* Changing the entire rendering system? - Flan
* Inputs improvements - Flan
* More vehicle work - Flan
* Driving inputs sorta laid out - Flan
* Lots of structures - Flan
* Initial structures - Flan
* Initial test export of World Wars - Flan
* Prevent a huge amount of hash map reallocation - Flan
* Don't copy the item stack if its already identical. This can be enough heap allocation to kill a low performance machine whenever a gun is used - Flan
* Fatal hit markers are red - Flan
* Fix scope overlay not hiding gun - Flan
* Sort recipes the same on client and server - Flan
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* Not much - Flan
* Update emissive renderer - PTTheta
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* #44 Use regular entity shaders (seems fine?) - Flan
* Fix some bad maths in sound dropoff - PTTheta
* Gunshot volume drop-off with range - PTTheta
* Stop bullet trail rendering when no hitscan - PTTheta
* 3rd person muzzle particles - PTTheta
* Anim events maybe - Flan
* Add Armour item, fix thrashing canPerformAction - Flan
* #39 Add config file - Flan
* Lots of laser render improvements - Flan
* Muzzle Flashes - Flan
* Laser in 3rd person - Flan
* Fix mag edits on server - Flan
* Don't hide gun model when remote player scopes - Flan
* Don't process release as press event on remote client - Flan
* Fix trait tooltips - Flan
* Some projectile tweaks and fix traits not loading - Flan
* Improved auto-build versioning and bumped things - Flan
* Make sure we ALWAYS send a release input to server - Flan
* Remove debug testing, so that snapshots snap every frame - Flan
* Fixes for Gun crafting in MP - Flan
* Avoid a call to getItems during client remote init - Flan
* Client remote connection is odd, it goes like - Flan
* - Wipe all tags - Flan
* - Evaluate recipe categories (we don't care, but default implementation is to check the input items) - Flan
* - Apply tags - Flan
* So we don't want to cache our matching items at this point - Flan
* #51 Multiplayer shooting is functional - Flan
* Supply strings for transform debug - Flan
* New hitbox code - Flan
* #49 Fix actions showing up on clients - Flan
* Fix mineable tag for gun mod table - Flan
* #50 Add some Attributes for modpacks - Flan
* Should be - Flan
* "flansmod.impact_damage_multiplier" - Flan
* "flansmod.splash_radius_multiplier" - Flan
* "flansmod.time_between_shots_multiplier" - Flan
* "flansmod.shot_spread_multiplier" - Flan
* "flansmod.vertical_recoil_multiplier" - Flan
* "flansmod.horizontal_recoil_multiplier" - Flan
* Modifier and ability refactor pretty functional - Flan
* More modifier updates, nearly ready - Flan
* Tidy up ground item poses - Flan
* New data exports for better stat accumulating - Flan
* Stacking maybe done aaaaa - Flan
* More ability and modifier rework - Flan
* Abilities rework, breaks data for old abilities - Flan
* Abilities refactor - not currently functional - Flan
* Fix grenade launcher crash - Flan
* (Version Bump!) Data changes to add ResLoc and ItemCollection - Flan
* Fix full auto triggering on server without shot data - Flan
* Finally fix tag exports - Flan
* Sort out messy partial tag setup - Flan
* Use contexts in the Workbench for better data retention - Flan
* #43 Fix trigger spam that was causing infinite damage - Flan
* Re-exported item tags #42 - Flan
* Fix comodification crash? Are players not threadsafe? - Flan
* Fix context invalidation on reconnecting to game - Flan
* Fix various publishing errors - Flan
* Ready for 1.20.1 release - Flan
* Build script stuff - Flan
* Context fixes - Flan
* Some content fixes and RepeatMode fixes - Flan
* Contexts much more stable and working in MP - Flan
* ContextHistory??? - Flan
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* Full Auto works much better - Flan
* Switch item rendering VertexBuffer to not conflict with other mods - PTTheta
* Fix nether travel and guns - Flan
* Fix bullet spread - Flan
* Some UI touchups - Flan
* JEI for distillation - Flan
* New content exports - Flan
* Particle shoot effects - PTTheta
* Fix tags - Flan
* Gun Fabricator full rework - Flan
* Reexport item models - Flan
* GunFabrication refactor to be a .json recipe - Flan
* Merge pull request #33 from FlansMods/1.20-jei - Flan
* Added JEI for part recipes - Flan
* Added JEI for part recipes - Flan
* Reexport for UV rotations - Flan
* Tons of transformstack fixes and content re-exports - Flan
* Additional gun components - PTTheta
* Tooltip improvements - Flan
* UI fixes - Flan
* Merge branch '1.20' of https://github.com/FlansMods/FlansModReloaded into 1.20 - Flan
* Lasers and sane transforms at last, also testing - Flan
* Sanitise id tag checks for compatibility for other mods - PTTheta
* ActionContexts looking tidy - Flan
* 1.20 Kinda Working - Flan
* Add totem grip - Flan
* Render types, like emissive or transparent - Flan
* Fix some more transform scaling with the new stack - Flan
* Fix mode toggling while ADS is active - Flan
* Apply isScoping mixin to reduce ADS sensitivity - Flan
* 45 deg sights and mode toggles - Flan
* TransformStack refactor and LASERS :D - Flan
* MP fixes (and hard wood ability) - Flan
* Abilities nearer feature complete #15 - Flan
* Boom, Headshot - add effects for abilities - Flan
* Abilities Initial Commit + Gun UUIDs - Flan
* Added 2h dual wield error to HUD #30 - Flan
* Eject bullets when decreasing mag size #27 - Flan
* Added paintcans and mag upgrades to loot #14 - Flan
* Add attachment crafting recipes #17 - Flan
* Update part crafting to be a vanilla category - Flan
* Fix first person shot origins - Flan
* Rename animations to avoid other anim mods - Flan
* Improvements to first person ADS - Flan
* #13 Smooth gun rotation and allow player to control with scroll wheel - Flan
* Added new stocks for F&C and HM - Flan
* Potential OptiFine fix - Flan
* Restrict inputs in gun fabrication to matching parts - Flan
* Remove old debug animation tool - Flan
* Fix some stale ShooterContexts after death - Flan
* Don't upload sources to CurseForge - Flan
* Fix crash on exit - Flan
* Crafting fixes - Flan
* Attempt at bullets rendering in trails - Flan
* Iterate APs when calculating eye line - Flan
* Attachments now restrict to weapon types - Flan
* Npc fixes - Flan
* Additional fixes and content tweaks - Flan
* Clean up duplicate APs - Flan
* Disable unfinished content - Flan
* - PhanTek Seeker - Flan
* - HM & FC stocks - Flan
* - TakeYourLifeInYourHands Totem Grip - Flan
* - Flashlights - Flan
* Add missing strings - Flan
* Add 3rd person poses for guns - Flan
* New SFX - Flan
* Stop Gradle crashing if you don't have the Curseforge token - Flan
* Fixed gun caching behaviour being jank - Flan
* Fix mods.toml files and pack.mcmeta files, builds look good - Flan
* Build scripts producing outputs - Flan

### 0.1


### 0.1.0

