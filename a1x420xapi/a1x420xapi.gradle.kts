version = "0.0.1"
plugins {
    id("com.github.johnrengelman.shadow")
}
project.extra["PluginName"] = "420xAPI"
project.extra["PluginDescription"] = "420xAPI"

configurations {
    create("moduleSpecific")
    create("kebabMuleSpecific")
}
dependencies {
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    compileOnly(group = "org.java-websocket", name = "Java-WebSocket", version = "1.5.4")
    //implementation(group = "net.dv8tion", name = "JDA", version = "5.0.0-beta.13")
    "moduleSpecific"(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    "moduleSpecific"(group = "org.java-websocket", name = "Java-WebSocket", version = "1.5.4")
    "kebabMuleSpecific"(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    "kebabMuleSpecific"(group = "org.java-websocket", name = "Java-WebSocket", version = "1.5.4")
    "kebabMuleSpecific"(group = "net.dv8tion", name = "JDA", version = "5.0.0-beta.13")

}
tasks {
    val shadowAPIJar by creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveClassifier.set("shadow")
        exclude("META-INF/versions/*/module-info.class")
        from(sourceSets.main.get().output) {
            exclude("META-INF/**")
        }
        configurations = listOf(project.configurations.getByName("moduleSpecific"))
    }
    val shadowKebabsAPIJar by creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveClassifier.set("shadow")
        exclude("META-INF/versions/*/module-info.class")
        exclude("module-info.class")
        from(sourceSets.main.get().output) {
            exclude("META-INF/**")
        }
        configurations = listOf(project.configurations.getByName("kebabMuleSpecific"))
    }
}