import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    java
    id("com.gradleup.shadow") version "8.3.10"
}

group = "wue"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val lwjglVersion = "3.3.6"
val jomlVersion  = "1.10.7"
val luajVersion  = "3.0.1"

// LWJGL ships per-platform native jars. Including all of them in the fat jar
// makes the resulting wue.jar runnable on every supported desktop OS without
// rebuild — LWJGL picks the right one at runtime via Platform.get().
val lwjglAllNatives = listOf(
    "natives-macos",
    "natives-macos-arm64",
    "natives-windows",
    "natives-windows-arm64",
    "natives-windows-x86",
    "natives-linux",
    "natives-linux-arm64"
)

val lwjglModules = listOf(
    "lwjgl",
    "lwjgl-glfw",
    "lwjgl-opengl",
    "lwjgl-openal",
    "lwjgl-stb"
)

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    lwjglModules.forEach { mod ->
        implementation("org.lwjgl:$mod")
        lwjglAllNatives.forEach { native ->
            runtimeOnly("org.lwjgl:$mod::$native")
        }
    }

    implementation("org.joml:joml:$jomlVersion")
    implementation("org.luaj:luaj-jse:$luajVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("wue.App")
    val osName = System.getProperty("os.name")
    val common = listOf("-Djava.awt.headless=true")
    applicationDefaultJvmArgs =
        if (osName.startsWith("Mac") || osName.startsWith("Darwin"))
            common + "-XstartOnFirstThread"
        else common
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("wue")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes("Main-Class" to "wue.App")
    }
    mergeServiceFiles()
    // Multiple LWJGL native jars carry overlapping META-INF entries; drop them.
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
}

// Stage a runnable distribution at ./wue/. Ships only the engine binary,
// launchers, and end-user README; the author drops their own scenario under
// ./wue/assets/scripts/main.lua at runtime.
tasks.register<Sync>("distWue") {
    group = "distribution"
    description = "Assembles a runnable distribution at ./wue/ (wue.jar + launchers + README)."
    dependsOn("shadowJar")

    into(layout.projectDirectory.dir("wue"))

    from(tasks.named<ShadowJar>("shadowJar").map { it.archiveFile })
    from(layout.projectDirectory.dir("distfiles")) {
        include("run.sh", "run.bat", "README.md")
    }

    doLast {
        val sh = layout.projectDirectory.file("wue/run.sh").asFile
        if (sh.exists()) sh.setExecutable(true, false)
    }
}
