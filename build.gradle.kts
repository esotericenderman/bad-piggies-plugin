import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.7.1"
  id("xyz.jpenilla.run-paper") version "2.3.0"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
  kotlin("jvm")
}

group = "dev.enderman"
version = "0.1.0"
description = "A plugin that implements some of the features from Rovio's \"Bad Piggies\" mobile game."

val javaVersion = 21;
val paperApiVersion = "1.21.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

dependencies {
    paperweight.paperDevBundle("$paperApiVersion-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileJava {
        options.release.set(javaVersion)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "com.example.MainKt"
    }

    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all of the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

bukkitPluginYaml {
    name = "BadPiggies"
    version = project.version.toString()
    main = "dev.enderman.minecraft.plugins.badpiggies.BadPiggiesPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("Esoteric Enderman")
    apiVersion = paperApiVersion
}
repositories {
    mavenCentral()
}
