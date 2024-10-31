plugins {
    java
}

val java9 = sourceSets.create("java9") {
    java {
        srcDir("src/main/java9")
    }
}

tasks.compileJava {
    javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.getByName<JavaCompile>(java9.compileJavaTaskName) {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })

    sourceCompatibility = "9"
    targetCompatibility = "9"
}

tasks.processResources {
    dependsOn(tasks[java9.classesTaskName])
    into("META-INF/versions/9") {
        from(java9.output)
    }
}
