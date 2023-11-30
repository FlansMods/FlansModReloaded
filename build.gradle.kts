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
    }

    dependencies {
        //minecraft 'net.minecraftforge:forge:1.19.3-44.1.0'
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.8.0.202006091008-r")
        classpath("org.apache.commons:commons-lang3:3.12.0")
        // compile against the JEI API but do not include it at runtime
        //compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-common-api:${jei_version}"))
        //compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge-api:${jei_version}"))
        // at runtime, use the full JEI jar for Forge
        //runtimeOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge:${jei_version}"))
    }
}

plugins {
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "5.1.+"
    id("java")
    id("com.matthewprenger.cursegradle") version "1.1.0"
}

val config: Properties = file("gradle.properties").inputStream().let {
    val prop = Properties()
    prop.load(it)
    return@let prop
}

val modBaseName = "flansmod"
val modCurseForgeID = config["flansmod.curseforge"] as String
val basicsCurseForgeID = config["basicparts.curseforge"] as String
val vendersCurseForgeID = config["vendersgame.curseforge"] as String





val mcVersion = config["minecraft.version"] as String
val mcFullVersion = "$mcVersion-${config["forge.version"]}"
val majorVersion = config["flansmod.version.major"] as String
val minorVersion = config["flansmod.version.minor"] as String
val jeiVersion = config["jei.version"] as String
val modVersionNoBuild = "$majorVersion.$minorVersion"

val git: Git = Git.open(projectDir)
val modVersion = "$modVersionNoBuild.${getBuildNumber()}"


// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))


version = "$mcVersion-$modVersion"
group = "com.flansmod"
configure<BasePluginConvention> {
    archivesBaseName = "flansmod-all"
}

configure<UserDevExtension> {
    mappings("official",  "1.19.3")
}

repositories {
    maven { //JEI
        name = "Progwml6 maven"
        setUrl("https://dvs1.progwml6.com/files/maven/")
    }
    maven { //JEI fallback
        name = "ModMaven"
        setUrl("modmaven.k-4u.nl")
    }
}

dependencies {
    //"deobfCompile"("mezz.jei:jei_$mcVersion:$jeiVersion")
    "minecraft"("net.minecraftforge:forge:$mcFullVersion")
}

// processResources
val Project.minecraft: UserDevExtension
    get() = extensions.getByName<UserDevExtension>("minecraft")

minecraft.runs.create("client") {
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

    baseName = "flansmod-all"

    version = mcVersion

    // replace stuff in mcmod.info, nothing else
    filesMatching("/mcmod.info") {
        expand(mapOf(
                "version" to project.version,
                "mcversion" to mcVersion
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
    classifier = "sources"
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
            include("**/data/flansmod/")
            include("**/data/forge/")
            include("**/com/flansmod/common/")
            include("**/com/flansmod/client/")
            include("**/com/flansmod/util/")
            include("**/META-INF/flansmod.toml")
            include("**/flansmod.mcmeta")
            include("**/flansmod.png")
            include("**/data/**/tags/**/*_flansmod.json")
        }
        rename { name ->
            if (name == "flansmod.toml")
                "mods.toml"
            else if(name == "flansmod.mcmeta")
                "pack.mcmeta"
            else if(name.endsWith("_flansmod.json"))
            {
                val newName = name.substring(0, name.lastIndexOf('_'))
                "$newName.json"
            }
            else name
        }
        includeEmptyDirs = false
        classifier = classfier
        version = "$mcVersion-$modVersion"
        getArchiveBaseName().set("flansmod")
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
            include("**/data/$modID/")
            include("**/com/flansmod/packs/$modNamespace/")
            include("**/META-INF/$modID.toml")
            include("**/$modID.mcmeta")
            include("**/$modID.png")
            include("**/data/**/tags/**/*_$modID.json")
        }
        rename { name ->
            if (name == "$modID.toml")
                "mods.toml"
            else if (name == "$modID.mcmeta")
                "pack.mcmeta"
            else if(name.endsWith("_$modID.json"))
            {
                val newName = name.substring(0, name.lastIndexOf('_'))
                "$newName.json"
            }
            else name
        }
        includeEmptyDirs = false
        classifier = classfier
        version = "$mcVersion-$modVersion"
        getArchiveBaseName().set(modID)
        destinationDirectory.set(file("$projectDir/build/output"))
        group = "flansmod build"
    }
}

val modSourceTask: Jar = modTask("BuildSourceFlansMod", sourceTask, "sources")
val modDevTask: Jar = modTask("BuildJarFlansMod", reobfTask, "")

val basicsSourceTask: Jar = repackTask("BuildSourceBasicParts", "flansbasicparts", "basics", sourceTask, "sources")
val basicsDevTask: Jar = repackTask("BuildJarBasicParts", "flansbasicparts", "basics", reobfTask, "")

val vendersSourceTask: Jar = repackTask("BuildSourceVendersGame", "flansvendersgame", "vendersgame", sourceTask, "sources")
val vendersDevTask: Jar = repackTask("BuildJarVendersGame", "flansvendersgame", "vendersgame", reobfTask, "")

artifacts {
    add("archives", reobfTask)
    add("archives", sourceTask)

    archives(modSourceTask.outputs.files.singleFile) { builtBy(modSourceTask) }
    archives(modDevTask.outputs.files.singleFile) { builtBy(modDevTask) }
    archives(vendersSourceTask.outputs.files.singleFile) { builtBy(vendersSourceTask) }
    archives(vendersDevTask.outputs.files.singleFile) { builtBy(vendersDevTask) }
    archives(basicsSourceTask.outputs.files.singleFile) { builtBy(basicsSourceTask) }
    archives(basicsDevTask.outputs.files.singleFile) { builtBy(basicsDevTask) }
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
            releaseType = "alpha"

            mainArtifact(main)
            addArtifact(src)

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
val basicPartsUploadTask     = createCurseForgeUploadTask(basicsCurseForgeID, basicsDevTask, basicsSourceTask)
val vendersUploadTask        = createCurseForgeUploadTask(vendersCurseForgeID, vendersDevTask, vendersSourceTask)

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
        dependsOn(vendersDevTask)
        dependsOn(basicsDevTask)
        group = "flansmod build"
    }
    tasks.create("BuildAllPackSources") {
        dependsOn(vendersSourceTask)
        dependsOn(basicsSourceTask)
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