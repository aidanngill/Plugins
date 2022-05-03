version = "1.1.6"

project.extra["PluginName"] = "Auto Blast Furnace"
project.extra["PluginDescription"] = "Automates the Blast Furnace minigame"

dependencies {
    compileOnly(group = "com.openosrs.externals", name = "iutils", version = "4.9.9+");
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Dependencies" to
                        arrayOf(
                            nameToId("iUtils")
                        ).joinToString(),
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
