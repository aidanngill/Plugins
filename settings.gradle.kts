rootProject.name = "R8 Plugins"

include(":autoblastfurnace")
include(":autoconstruction")
include(":autolog")
include(":autoteleblock")
include(":autozmi")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
