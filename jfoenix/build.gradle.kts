plugins {
    java
}

tasks.compileJava {
    javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
