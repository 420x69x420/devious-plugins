version = "0.0.2"

project.extra["PluginName"] = "MuleF2P"
project.extra["PluginDescription"] = "MuleF2P"
dependencies {
    implementation(project(":a1x420xapi"))
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    implementation(group = "org.java-websocket", name = "Java-WebSocket", version = "1.5.2")

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
