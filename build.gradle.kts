import ProjectVersions.unethicaliteVersion
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

project.extra["GithubUrl"] = "https://github.com/melxin/devious-plugins-extended"
project.extra["GithubUserName"] = "melxin"
project.extra["GithubRepoName"] = "devious-plugins-extended"

apply<BootstrapPlugin>()
allprojects {
    group = "net.unethicalite"

    project.extra["PluginProvider"] = "melxin"
    project.extra["ProjectSupportUrl"] = ""
    project.extra["PluginLicense"] = "3-Clause BSD License"

    apply<JavaPlugin>()
    apply(plugin = "java-library")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)

        compileOnly("net.unethicalite:http-api:$unethicaliteVersion+")
        compileOnly("net.unethicalite:runelite-api:$unethicaliteVersion+")
        compileOnly("net.unethicalite:runelite-client:$unethicaliteVersion+")
        compileOnly("net.unethicalite.rs:runescape-api:$unethicaliteVersion+")

        compileOnly(Libraries.guice)
        compileOnly(Libraries.javax)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
        withType<Jar> {
            doLast {
                copy {
                    from("./build/libs/")
                    into(System.getProperty("user.home")+"/.openosrs/plugins/release-the-plugins/")
                    exclude("unethicalite-plugins.jar")
                    exclude("a1x420xapi*.jar")
                }
            }

        }
        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }
        compileKotlin {
            kotlinOptions.jvmTarget = "11"
        }
    }
}

