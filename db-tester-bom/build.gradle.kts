plugins {
    `java-platform`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester BOM - Bill of Materials for DB Tester framework"

javaPlatform {
    // Allow importing other BOMs/platforms
    allowDependencies()
}

dependencies {
    // Version constraints for all DB Tester modules
    constraints {
        api(project(":db-tester-core"))
        api(project(":db-tester-junit"))
        api(project(":db-tester-spock"))
        api(project(":db-tester-junit-spring-boot-starter"))
        api(project(":db-tester-spock-spring-boot-starter"))
    }
}

mavenPublishing {
    configure(com.vanniktech.maven.publish.JavaPlatform())

    pom {
        name = "DB Tester BOM"
        description = "Bill of Materials for DB Tester framework - manages versions for all modules"
    }
}
