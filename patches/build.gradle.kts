group = "app.o0b.morphe-patches"

patches {
    about {
        name = "o0b Patches"
        description = "Octopi Launcher Pro unlock (1.92)"
        source = "https://github.com/o0b/morphe-patches"
        author = "o0b"
        contact = "https://github.com/o0b"
        website = "https://morphe.software/add-source?github=o0b/morphe-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
