version = "0.0.2"

project.extra["PluginName"] = "Sharks"
project.extra["PluginDescription"] = "Sharks"
dependencies {
    implementation(project(":a1x420xapi"))
}
tasks {
    val jar by existing(org.gradle.jvm.tasks.Jar::class) {
        dependsOn(":a1x420xapi:shadowAPIJar")

        doFirst {
            val apiJar = project(":a1x420xapi").tasks.named("shadowAPIJar").get().outputs.files.singleFile
            from(zipTree(apiJar))
        }
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
