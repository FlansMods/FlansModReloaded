import com.google.gson.JsonObject
import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
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

//fun minecraft(configure: UserBaseExtension.() -> Unit) = project.configure(configure)


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
tasks.withType<Jar> {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)

    baseName = "flansmod-all"

    // replace stuff in mcmod.info, nothing else
    filesMatching("/mcmod.info") {
        expand(mapOf(
                "version" to project.version,
                "mcversion" to "1.12.2"
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
        attributes(mapOf("FMLAT" to "gregtech_at.cfg",
                "FMLCorePlugin" to "gregtech.common.asm.GTCELoadingPlugin",
                "FMLCorePluginContainsFMLMod" to "true"))
    }
}

val sourceTask: Jar = tasks.create("source", Jar::class.java) {
    from(sourceSets["main"].allSource)
    classifier = "sources"
}

val devTask: Jar = tasks.create("dev", Jar::class.java) {
    from(sourceSets["main"].output)
    classifier = "dev"
}

// --------------------------- CORE MOD -----------------------------
val modSourceTask: Jar = tasks.create("modSource", Jar::class.java) {
    dependsOn(sourceTask)
    from(zipTree(sourceTask.outputs.files.singleFile))
    {
        include("assets/flansmod/")
        include("assets/forge/")
        include("data/flansmod/")
        include("data/forge/")
        include("com/flansmod/common/")
        include("com/flansmod/client/")
        include("com/flansmod/util/")
    }
    classifier = "sources"
    getArchiveBaseName().set("flansmod")
    destinationDirectory.set(file("$projectDir/build/output"))
}
val modDevTask: Jar = tasks.create("modDev", Jar::class.java) {
    dependsOn(devTask)
    from(zipTree(devTask.outputs.files.singleFile))
    {
        include("assets/flansmod/")
        include("assets/forge/")
        include("data/flansmod/")
        include("data/forge/")
        include("com/flansmod/common/")
        include("com/flansmod/client/")
        include("com/flansmod/util/")
    }
    classifier = "dev"
    getArchiveBaseName().set("flansmod")
    destinationDirectory.set(file("$projectDir/build/output"))
}

// ----------------------------- BASIC PARTS ------------------------------
val basicsSourceTask: Jar = tasks.create("basicsSource", Jar::class.java) {
    dependsOn(sourceTask)
    from(zipTree(sourceTask.outputs.files.singleFile))
    {
        include("assets/flansbasicparts/")
        include("data/flansbasicparts/")
        include("com/flansmod/packs/basics/")
    }
    classifier = "sources"
    getArchiveBaseName().set("flansbasicparts")
    destinationDirectory.set(file("$projectDir/build/output"))
}
val basicsDevTask: Jar = tasks.create("basicsDev", Jar::class.java) {
    dependsOn(devTask)
    from(zipTree(devTask.outputs.files.singleFile))
    {
        include("assets/flansbasicparts/")
        include("data/flansbasicparts/")
        include("com/flansmod/packs/basics/")
    }
    classifier = "dev"
    getArchiveBaseName().set("flansbasicparts")
    destinationDirectory.set(file("$projectDir/build/output"))
}

// ----------------------------- VENDER'S GAME ------------------------------
val vendersSourceTask: Jar = tasks.create("vendersSource", Jar::class.java) {
    dependsOn(sourceTask)
    from(zipTree(sourceTask.outputs.files.singleFile))
    {
        include("assets/flansvendersgame/")
        include("data/flansvendersgame/")
        include("com/flansmod/packs/vendersgame/")
    }
    classifier = "sources"
    getArchiveBaseName().set("flansvendersgame")
    destinationDirectory.set(file("$projectDir/build/output"))
}
val vendersDevTask: Jar = tasks.create("vendersDev", Jar::class.java) {
    dependsOn(devTask)
    from(zipTree(devTask.outputs.files.singleFile))
    {
        include("assets/flansvendersgame/")
        include("data/flansvendersgame/")
        include("com/flansmod/packs/vendersgame/")
    }
    classifier = "dev"
    getArchiveBaseName().set("flansvendersgame")
    destinationDirectory.set(file("$projectDir/build/output"))
}



artifacts {
    add("archives", jar)
    add("archives", sourceTask)

    archives(modSourceTask.outputs.files.singleFile) { builtBy(modSourceTask) }
    archives(modDevTask.outputs.files.singleFile) { builtBy(modDevTask) }
    archives(vendersSourceTask.outputs.files.singleFile) { builtBy(vendersSourceTask) }
    archives(vendersDevTask.outputs.files.singleFile) { builtBy(vendersDevTask) }
    archives(basicsSourceTask.outputs.files.singleFile) { builtBy(basicsSourceTask) }
    archives(basicsDevTask.outputs.files.singleFile) { builtBy(basicsDevTask) }
}

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

//val curseforgeProject = configureCurseforgeTask()
//curseforgeProject?.uploadTask?.outputs?.upToDateWhen { false }

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
    val startCommitId = ObjectId.fromString("a09d3c3091d061c251ddfed6e6242f0ff9c37d8d")
    gitLog.addRange(startCommitId, headCommitId)
    return gitLog.call().toList().size.toString()
}

publishing {
    publications {
        create("FlansModPublication", MavenPublication::class.java) {
            groupId = project.group as String
            artifactId = the<BasePluginConvention>().archivesBaseName
            version = project.version as String

            artifact(jar)
            artifact(sourceTask)
        }
    }
}