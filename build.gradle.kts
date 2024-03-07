import org.spongepowered.asm.gradle.plugins.MixinExtension
import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.apache.commons.lang3.StringUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import java.util.*
import java.util.Arrays.asList
import java.util.zip.ZipOutputStream
import javax.xml.stream.events.Namespace

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "jitpack"
            setUrl("https://jitpack.io")
        }
        maven {
            name = "forge"
            setUrl("https://maven.minecraftforge.net/")
        }
        maven {
            name = "sponge"
            setUrl("https://repo.spongepowered.org/repository/maven-public/")
        }
    }

    dependencies {
        //minecraft 'net.minecraftforge:forge:1.19.3-44.1.0'
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.8.0.202006091008-r")
        classpath("org.apache.commons:commons-lang3:3.12.0")
        classpath("org.spongepowered:mixingradle:0.7.+")
    }
}

plugins {
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("java")
    id("com.matthewprenger.cursegradle") version "1.1.0"
}
apply(plugin = "org.spongepowered.mixin")

val Project.mixin: MixinExtension
    get() = extensions.getByType()
mixin.run {
    add(sourceSets.main.get(), "flansmod.refmap.json")
    config("flansmod.mixins.json")
}

val config: Properties = file("gradle.properties").inputStream().let {
    val prop = Properties()
    prop.load(it)
    return@let prop
}

val git: Git = Git.open(projectDir)
val modBaseName = "flansmod"

// FML Versioning, different to Forge?
val fmlRange = config["fml.version.range"] as String

// Forge versioning
val forgeVersion = config["forge.version"] as String
val forgeVersionMax = config["forge.version.max"] as String
val forgeVersionRange = "[$forgeVersion, $forgeVersionMax)"

// Minecraft versioning
val mcVersion = config["minecraft.version"] as String
val mcVersionMax = config["minecraft.version.max"] as String
val mcVersionRange = "[$mcVersion, $mcVersionMax)"
val mcFullVersion = "$mcVersion-${config["forge.version"]}"

// JEI versioning
val jeiVersion = config["jei.version"] as String
val jeiVersionMax = config["jei.version.max"] as String
val jeiVersionRange = "[$jeiVersion, $jeiVersionMax)"

// Mod versioning
val majorVersion = config["flansmod.version.major"] as String
val minorVersion = config["flansmod.version.minor"] as String
val modVersionNoBuild = "$majorVersion.$minorVersion"
val modVersion = "$modVersionNoBuild.${getBuildNumber()}"
val modCurseForgeID = config["flansmod.curseforge"] as String

val packModVersionMin = config["packs.flansmod.version.min"] as String
val packModVersionMax = config["packs.flansmod.version.max"] as String
val packModVersionRange = "[$packModVersionMin, $packModVersionMax)"

// Basic Parts versioning
val basicsVersionMajor = config["basics.version.major"] as String
val basicsVersionMinor = config["basics.version.minor"] as String
val basicsVersionNoBuild = "$basicsVersionMajor.$basicsVersionMinor";
val basicsVersion = "$basicsVersionNoBuild.${getBuildNumber()}"
val basicsCurseForgeID = config["basics.curseforge"] as String

// Vender's Game versioning
val vendersVersionMajor = config["venders.version.major"] as String
val vendersVersionMinor = config["venders.version.minor"] as String
val vendersVersionNoBuild = "$vendersVersionMajor.$vendersVersionMinor";
val vendersVersion = "$vendersVersionNoBuild.${getBuildNumber()}"
val vendersCurseForgeID = config["venders.curseforge"] as String




// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))


version = "$mcVersion-$modVersion"
group = "com.flansmod"
configure<BasePluginConvention> {
    archivesBaseName = "flansmod-all"
}

configure<UserDevExtension> {
    mappings("official",  "1.20.1")
}

repositories {
    maven {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
        setUrl("https://maven.blamejared.com/")
    }
}

dependencies {
    //"deobfCompile"("mezz.jei:jei_$mcVersion:$jeiVersion")
    "minecraft"("net.minecraftforge:forge:$mcFullVersion")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    // compile against the JEI API but do not include it at runtime
    compileOnly(fg.deobf("mezz.jei:jei-${mcVersion}-common-api:${jeiVersion}"))
    compileOnly(fg.deobf("mezz.jei:jei-${mcVersion}-forge-api:${jeiVersion}"))
    // at runtime, use the full JEI jar for Forge
    runtimeOnly(fg.deobf("mezz.jei:jei-${mcVersion}-forge:${jeiVersion}"))
}

// processResources
val Project.minecraft: UserDevExtension
    get() = extensions.getByName<UserDevExtension>("minecraft")

minecraft.runs.create("client") {
    workingDirectory = "run"
    jvmArgs = listOf( "-XX:+AllowRedefinitionToAddDeleteMethods" )
    properties["forge.logging.markers"] = "REGISTRIES"
    properties["forge.logging.console.level"] = "debug"
    properties["forge.enabledGameTestNamespaces"] = "flansmod"
    mods.create("flansmod") {
        sources.add(sourceSets.main.get())
    }
    mods.create("flansbasicparts") {
        sources.add(sourceSets.main.get())
    }
    mods.create("flansvendersgame") {
        sources.add(sourceSets.main.get())
    }
}

minecraft.runs.create("server") {
    workingDirectory = "run"
    properties["forge.logging.markers"] = "REGISTRIES"
    properties["forge.logging.console.level"] = "debug"
    properties["forge.enabledGameTestNamespaces"] = "flansmod"
    mods.create("flansmod") {
        sources.add(sourceSets.main.get())
    }
    mods.create("flansbasicparts") {
        sources.add(sourceSets.main.get())
    }
    mods.create("flansvendersgame") {
        sources.add(sourceSets.main.get())
    }
}


tasks.withType<Jar> {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)

    archiveBaseName.set("flansmod-all")

    archiveVersion.set(mcVersion)

    // replace stuff in mcmod.info and mods.toml
    filesMatching(listOf("*/flansmod.toml", "*/flansvendersgame.toml", "*/flansbasicparts.toml"))
    {
        expand(
            mapOf(
                    "fmlrange" to fmlRange,
                    "flansmodversion" to modVersionNoBuild,
                    "flansvendersgameversion" to vendersVersion,
                    "flansbasicpartsversion" to basicsVersion,
                    "mcversionrange" to mcVersionRange,
                    "forgeversionrange" to forgeVersionRange,
                    "flansmodversionrange" to packModVersionRange,
                    "jeiversionrange" to jeiVersionRange,

        ))
    }
}

// workaround for userdev bug
tasks.create("copyResourceToClasses", Copy::class) {
    tasks.classes.get().dependsOn(this)
    dependsOn(tasks.processResources.get())
    onlyIf { gradle.taskGraph.hasTask(tasks.getByName("prepareRuns")) }
    into("$buildDir/classes/kotlin/main")
    from(tasks.processResources.get().destinationDir)
}

val jar: Jar by tasks
jar.apply {
    manifest {
        attributes(mapOf("FMLAT" to "flansmod_at.cfg",
                //"FMLCorePlugin" to "flansmod.common.asm.FlansModLoadingPlugin",
                "FMLCorePluginContainsFMLMod" to "true"))
    }
}

val reobfTask = jar.finalizedBy("reobfJar")

// ---------------------- BUILD AND REPACK TASKS ---------------------------
val sourceTask: Jar = tasks.create("source", Jar::class.java) {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val devTask: Jar = tasks.create("dev", Jar::class.java) {
    from(sourceSets["main"].output)
}







fun modTask(taskName: String, srcTask: Task, classfier: String): Jar {
    return tasks.create(taskName, Jar::class.java) {
        dependsOn(srcTask)
        from(zipTree(srcTask.outputs.files.singleFile))
        {
            include("**/assets/flansmod/")
            include("**/assets/forge/")
            include("**/assets/minecraft")
            include("**/com/flansmod/common/")
            include("**/com/flansmod/client/")
            include("**/com/flansmod/util/")
            include("**/META-INF/flansmod.toml")
            include("**/flansmod.mcmeta")
            include("**/flansmod.png")

            // Special handling: Don't include dev tags, include (and rename) the partial tag files
            include("**/data/flansmod/")
            exclude("**/data/flansmod/tags/")
            include("**/data/**/tags/partial_tags/flansmod/**/*.json")

            // Special handling: Same for loot_modifiers
            include("**/data/forge/")
            exclude("**/data/forge/loot_modifiers/")
            include("**/data/forge/partial_loot_modifiers/flansmod/")
        }
        rename { name ->
            if (name == "flansmod.toml")
                "mods.toml"
            else if(name == "flansmod.mcmeta")
                "pack.mcmeta"
            else if(name.contains("/partial_tags/flansmod/"))
                name.replace("/partial_tags/flansmod/", "/tags/")
            else if(name.contains("/partial_loot_modifiers/flansmod/"))
                name.replace("/partial_loot_modifiers/flansmod/", "/loot_modifiers/")
            else name
        }
        includeEmptyDirs = false
        archiveClassifier.set(classfier)
        archiveVersion.set("$mcVersion-$modVersion")
        archiveBaseName.set("flansmod")
        destinationDirectory.set(file("$projectDir/build/output"))
        group = "flansmod build"
    }
}

fun repackTask(taskName: String, modID: String, modNamespace: String, srcTask: Task, classfier: String): Jar {
    return tasks.create(taskName, Jar::class.java) {
        dependsOn(srcTask)
        from(zipTree(srcTask.outputs.files.singleFile))
        {

            include("**/assets/$modID/")
            include("**/com/flansmod/packs/$modNamespace/")
            include("**/META-INF/$modID.toml")
            include("**/$modID.mcmeta")
            include("**/$modID.png")

            // We don't want our "compiled" tags, these contain every modded tag in our workspace and are for dev use
            include("**/data/$modID/")
            exclude("**/data/$modID/tags/")
            include("**/data/**/partial_tags/$modID/**/*_.json")

            // Same applies to loot modifiers, if we have any
            include("**/data/forge/partial_loot_modifiers/$modID/")
        }
        rename { name ->
            if (name == "$modID.toml")
                "mods.toml"
            else if (name == "$modID.mcmeta")
                "pack.mcmeta"
            else if(name.contains("/partial_tags/$modID/"))
                name.replace("/partial_tags/$modID/", "/tags/")
            else if(name.contains("/partial_loot_modifiers/$modID/"))
                name.replace("/partial_loot_modifiers/$modID/", "/loot_modifiers/")
            else name
        }
        includeEmptyDirs = false
        archiveClassifier.set(classfier)
        archiveVersion.set("$mcVersion-$modVersion")
        archiveBaseName.set(modID)
        destinationDirectory.set(file("$projectDir/build/output"))
        group = "flansmod build"
    }
}


val cleanTask: Delete = tasks.create("CleanExpandedFolder", Delete::class.java) {
    delete(
            fileTree("$projectDir/build/expanded"),
            fileTree("$projectDir/build/pack"))
    group = "flansmod internal"
}
fun expandJarTask(srcTask: Task, completeFolder: File): Copy {

    return tasks.create("Expand_${srcTask.name}", Copy::class.java) {
        dependsOn(cleanTask)
        dependsOn(srcTask)
        from(zipTree(srcTask.outputs.files.singleFile))
        {

        }
        includeEmptyDirs = false
        destinationDir = completeFolder
        group = "flansmod internal"
    }
}
fun gatherFilesForPackTask(modID: String, modNamespace: String, classifier:String, expandTask: Task, expandedDir: File, outputDir:File): Copy {
    return tasks.create("GatherFiles_${modID}_${classifier}", Copy::class.java) {
        dependsOn(expandTask)
        into("")
        {
            from(fileTree(expandedDir))
            {
                include("**/assets/$modID/")
                include("**/data/$modID/")
                include("**/com/flansmod/packs/$modNamespace/")
                include("**/$modID.mcmeta")
                include("**/$modID.png")
                include("**/META-INF/$modID.toml")

                exclude("**/data/$modID/tags/")
                exclude("**/data/$modID/loot_modifiers/")
                exclude("**/data/$modID/partial_data/")
            }
            rename { name ->
                if (name == "$modID.toml")
                    "mods.toml"
                else if (name == "$modID.mcmeta")
                    "pack.mcmeta"
                else name
            }
        }
        into("data")
        {
            from(fileTree(file("$expandedDir/data/$modID/partial_data/")))
            {
            }
        }
        destinationDir = outputDir
        includeEmptyDirs = false
        group = "flansmod internal"
    }
}
fun jarPackTask(modID: String, modNamespace: String, classifier:String, expandTask: Task, expandedDir: File): Jar {
    var suffix: String = classifier
    if(classifier == "dev")
        suffix = ""
    return tasks.create("BuildJar_${modID}_${classifier}", Jar::class.java) {
        val filteredDir:File = file("$projectDir/build/pack/$modID/$classifier")
        val gatherTask: Copy = gatherFilesForPackTask(modID, modNamespace, classifier, expandTask, expandedDir, filteredDir)
        dependsOn(gatherTask)
        from(fileTree(filteredDir))
        {

        }
        includeEmptyDirs = false
        archiveClassifier.set(suffix)
        archiveVersion.set("$mcVersion-$modVersion")
        archiveBaseName.set(modID)
        destinationDirectory.set(file("$projectDir/build/output"))
        group = "flansmod internal"
    }
}
fun gatherFilesForModTask(classifier:String, expandTask: Task, expandedDir: File, outputDir:File): Copy {
    return tasks.create("GatherFiles_flansmod_${classifier}", Copy::class.java) {
        dependsOn(expandTask)
        into("")
        {
            from(fileTree(expandedDir))
            {
                include("**/assets/flansmod/")
                include("**/assets/minecraft/")
                include("**/data/flansmod/")
                include("**/com/flansmod/")
                include("**/flansmod.mcmeta")
                include("**/flansmod.png")
                include("**/META-INF/flansmod.toml")

                exclude("**/com/flansmod/packs")
                exclude("**/data/flansmod/tags/")
                exclude("**/data/flansmod/partial_data/")
                exclude("**/data/flansmod/loot_modifiers/")
            }
            rename { name ->
                if (name == "flansmod.toml")
                    "mods.toml"
                else if (name == "flansmod.mcmeta")
                    "pack.mcmeta"
                else name
            }
        }
        into("data")
        {
            from(fileTree(file("$expandedDir/data/flansmod/partial_data/")))
            {
            }
        }
        destinationDir = outputDir
        includeEmptyDirs = false
        group = "flansmod internal"
    }
}
fun jarModTask(classifier:String, expandTask: Task, expandedDir: File): Jar {

    var suffix: String = classifier
    if(classifier == "dev")
        suffix = ""
    return tasks.create("BuildJar_flansmod_${classifier}", Jar::class.java) {
        val filteredDir:File = file("$projectDir/build/pack/flansmod/$classifier/")
        val gatherTask: Copy = gatherFilesForModTask(classifier, expandTask, expandedDir, filteredDir)
        dependsOn(gatherTask)
        from(fileTree(filteredDir))
        {

        }
        includeEmptyDirs = false
        archiveClassifier.set(suffix)
        archiveVersion.set("$mcVersion-$modVersion")
        archiveBaseName.set("flansmod")
        destinationDirectory.set(file("$projectDir/build/output"))
        group = "flansmod internal"
    }
}



val expandedSourceDir = file("$projectDir/build/expanded/sources")
val expandedDevDir = file("$projectDir/build/expanded/dev")
val expandSourceTask: Copy = expandJarTask(sourceTask, expandedSourceDir)
val expandDevTask: Copy = expandJarTask(reobfTask, expandedDevDir)

fun CreateAllPackTasks(modID: String, modNamespace: String): Pair<Jar, Jar>
{
    return Pair(jarPackTask(modID, modNamespace, "sources", expandSourceTask, expandedSourceDir),
                jarPackTask(modID, modNamespace, "dev", expandDevTask, expandedDevDir))
}



val basicsTasks = CreateAllPackTasks("flansbasicparts", "basics")
val vendersTasks = CreateAllPackTasks("flansvendersgame", "vendersgame")

val modSourceTask: Jar = jarModTask("sources", expandSourceTask, expandedSourceDir)
val modDevTask: Jar = jarModTask("dev", expandDevTask, expandedDevDir)

artifacts {
    add("archives", reobfTask)
    add("archives", sourceTask)

    archives(modSourceTask.outputs.files.singleFile) { builtBy(modSourceTask) }
    archives(modDevTask.outputs.files.singleFile) { builtBy(modDevTask) }
    archives(basicsTasks.first.outputs.files.singleFile) { builtBy(basicsTasks.first) }
    archives(basicsTasks.second.outputs.files.singleFile) { builtBy(basicsTasks.second) }
    archives(vendersTasks.first.outputs.files.singleFile) { builtBy(vendersTasks.first) }
    archives(vendersTasks.second.outputs.files.singleFile) { builtBy(vendersTasks.second) }
}

// -------------------------------- GIT HUB -----------------------------------
tasks.create("generateChangelog") {
    doLast {
        val file = file("CHANGELOG.md")
        val fileContents = StringBuilder(file.readText(Charsets.UTF_8))
        val versionHeader = "\n### $modVersionNoBuild\n"
        if (fileContents.contains(versionHeader)) return@doLast
        val firstNewline = fileContents.indexOf('\n')
        val changelog = getActualChangeList()
        val insertText = "\n$versionHeader$changelog"
        fileContents.insert(firstNewline, insertText)
        file.writeText(fileContents.toString(), Charsets.UTF_8)
    }
}

fun resolveVersionChangelog(): String {
    val changeLogLines = file("CHANGELOG.md").readLines(Charsets.UTF_8)
    val versionHeader = "### $modVersionNoBuild"
    val startLineIndex = changeLogLines.indexOf(versionHeader)
    if (startLineIndex == -1) {
        return "No changelog provided"
    }
    val changelogBuilder = StringBuilder()
    var lineIndex = startLineIndex
    while (lineIndex < changeLogLines.size) {
        val changelogLine = changeLogLines[lineIndex]
        if (changelogLine.isEmpty()) break
        changelogBuilder.append(changelogLine).append("\n")
        lineIndex++
    }
    return changelogBuilder.toString()
}


tasks["build"].dependsOn("generateChangelog")

fun getPrettyCommitDescription(commit: RevCommit): String {
    val closePattern = Regex("(Closes|Fixes) #[0-9]*\\.?")
    val author = commit.authorIdent.name
    val message = commit.fullMessage
            //need to remove messages that close issues from changelog
            .replace(closePattern, "")
            //cut multiple newlines, format them all to linux line endings
            .replace(Regex("(\r?\n){1,}"), "\n")
            //cut squashed commit sub-commits descriptions
            .replace(Regex("\\* [^\\n]*\\n"), "")
            //split commit message on lines, trim each one
            .split('\n').asSequence().map { it.trim() }
            .filterNot { it.isBlank() }
            //cut out lines that are related to merges
            .filter { !it.startsWith("Merge remote-tracking branch") }
            //cut lines that carry too little information
            .filter { it.length > 3 }
            //captialize each line, add . at the end if it's not there
            .map { StringUtils.capitalize(it) }
            .map { if (it.endsWith('.')) it.substring(0, it.length - 1) else it }
            //append author to each line
            .map { "* $it - $author" }.toList()
    return message.joinToString( separator = "\n")
}

fun getCommitFromTag(revWalk: RevWalk, tagObject: RevObject) : RevCommit? {
    return when (tagObject) {
        is RevCommit -> tagObject
        is RevTag -> getCommitFromTag(revWalk, revWalk.parseAny(tagObject.`object`))
        else -> error("Encountered version tag pointing to a non-commit object: $tagObject")
    }
}

fun getActualTagName(tagName: String): String {
    val latestSlash = tagName.lastIndexOf('/')
    return tagName.substring(latestSlash + 1)
}

fun getActualChangeList(): String {
    val revWalk = RevWalk(git.repository)
    val latestTagCommit = git.tagList().call()
            .filter { getActualTagName(it.name).startsWith("v") }
            .mapNotNull { getCommitFromTag(revWalk, revWalk.parseAny(it.objectId)) }
            .maxBy { it.commitTime } ?: error("No previous release version tag found")

    val gitLog = git.log()
    val headCommitId = git.repository.resolve(Constants.HEAD)
    gitLog.addRange(latestTagCommit, headCommitId)
    val commitsBetween = gitLog.call().map { getPrettyCommitDescription(it) }.filterNot { it.isBlank() }
    return commitsBetween.joinToString(separator = "\n")
}

fun getBuildNumber(): String {
    val gitLog = git.log()
    val headCommitId = git.repository.resolve(Constants.HEAD)
    val startCommitId = ObjectId.fromString("a09d3c3091d061c251ddfed6e6242f0ff9c37d8d") // FlansMod119 first commit hash
    gitLog.addRange(startCommitId, headCommitId)
    return gitLog.call().toList().size.toString()
}


// -------------------------------- CURSE FORGE -----------------------------------
fun CurseExtension.project(config: CurseProject.() -> Unit) = CurseProject().also {
    it.config()
    curseProjects.add(it)
}
// has to be called after addArtifact ¯\_(ツ)_/¯
fun CurseProject.relations(config: CurseRelation.() -> Unit) = CurseRelation().also {

    it.config()

    additionalArtifacts.forEach { artifact ->
        artifact.curseRelations = it
    }

    mainArtifact.curseRelations = it
}

fun configureCurseforgeTask(): CurseProject? {
    if (System.getenv("CURSE_API_KEY") != null) {
        val extension = curseforge
        extension.apiKey = System.getenv("CURSE_API_KEY")
        extension.curseGradleOptions.forgeGradleIntegration = false
        return extension.project {
            apiKey = System.getenv("CURSE_API_KEY")
            id = modCurseForgeID
            addGameVersion(mcVersion)
            changelog = file("CHANGELOG.md")
            changelogType = "markdown"
            releaseType = "alpha"

            mainArtifact(modDevTask)
            addArtifact(modSourceTask)

            relations {
                optionalDependency("jei")
            }
        }
    } else {
        println("Skipping curseforge task as there is no api key in the environment")
        return null
    }
}

fun initCurseForgeExtension(): CurseExtension? {
    return if (System.getenv("CURSE_API_KEY") != null) {
        val extension = curseforge
        extension.apiKey = System.getenv("CURSE_API_KEY")
        extension.curseGradleOptions.forgeGradleIntegration = false
        extension
    } else {
        println("Skipping curseforge task as there is no api key in the environment")
        null
    }
}

fun createCurseForgeUploadTask(curseForgeID: String, main: Jar, src: Jar): CurseProject? {
    val curseExtension = initCurseForgeExtension()
    if(curseExtension != null)
    {
        val curseForgeProject = curseExtension.project {
            apiKey = System.getenv("CURSE_API_KEY")
            id = curseForgeID
            addGameVersion(mcVersion)
            changelog = file("CHANGELOG.md")
            changelogType = "markdown"
            releaseType = "beta"

            mainArtifact(main)
            //addArtifact(src) // Don't add sources, it is confusing people

            relations {
                optionalDependency("jei")
            }
        }

        curseForgeProject.uploadTask?.outputs?.upToDateWhen { false }
        return curseForgeProject
    }
    else return null
}



//if (curseforgeProject != null) {
   // notificationTask.dependsOn("curseforge")
//}

val modUploadTask            = createCurseForgeUploadTask(modCurseForgeID, modDevTask, modSourceTask)
val basicPartsUploadTask     = createCurseForgeUploadTask(basicsCurseForgeID, basicsTasks.second, basicsTasks.first)
val vendersUploadTask        = createCurseForgeUploadTask(vendersCurseForgeID, vendersTasks.second, vendersTasks.first)

afterEvaluate {
    if(modUploadTask?.uploadTask != null)
        tasks.create("PublishFlansMod") {
            dependsOn(modUploadTask.uploadTask)
            group = "flansmod publish"
        }
    if(basicPartsUploadTask?.uploadTask != null)
        tasks.create("PublishBasicParts") {
            dependsOn(basicPartsUploadTask.uploadTask)
            group = "flansmod publish"
        }
    if(vendersUploadTask?.uploadTask != null)
        tasks.create("PublishVendersGame") {
            dependsOn(vendersUploadTask.uploadTask)
            group = "flansmod publish"
        }

    if(tasks.findByPath("PublishBasicParts") != null) {
        tasks.create("PublishAllPacks") {
            dependsOn(tasks.getByName("PublishBasicParts"))
            dependsOn(tasks.getByName("PublishVendersGame"))
            group = "flansmod publish"
        }
        tasks.create("PublishFlansModAndAllPacks") {
            dependsOn(tasks.getByName("PublishFlansMod"))
            dependsOn(tasks.getByName("PublishAllPacks"))
            group = "flansmod publish"
        }
    }

    tasks.create("BuildAllPackJars") {
        dependsOn(vendersTasks.second)
        dependsOn(basicsTasks.second)
        group = "flansmod build"
    }
    tasks.create("BuildAllPackSources") {
        dependsOn(vendersTasks.first)
        dependsOn(basicsTasks.first)
        group = "flansmod build"
    }
    tasks.create("BuildAllJars") {
        dependsOn(modDevTask)
        dependsOn(tasks.getByName("BuildAllPackJars"))
        group = "flansmod build"
    }
    tasks.create("BuildAllSources") {
        dependsOn(modSourceTask)
        dependsOn(tasks.getByName("BuildAllPackSources"))
        group = "flansmod build"
    }
    tasks.create("BuildAll") {
        dependsOn(tasks.getByName("BuildAllJars"))
        dependsOn(tasks.getByName("BuildAllSources"))
        group = "flansmod build"
    }

}

publishing {
    publications {
        create("FlansModPublication", MavenPublication::class.java) {
            groupId = project.group as String
            artifactId = "flansmod"
            version = project.version as String

            artifact(jar)
            artifact(sourceTask)
        }
    }
}