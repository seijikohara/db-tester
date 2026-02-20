plugins {
    `java-platform`
    id("dbtester.publishing")
}

group = rootProject.group
version = rootProject.version

description = "DB Tester BOM - Bill of Materials for DB Tester framework"

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":db-tester-api"))
        api(project(":db-tester-core"))
        api(project(":db-tester-spring-support"))
        api(project(":db-tester-junit"))
        api(project(":db-tester-spock"))
        api(project(":db-tester-kotest"))
        api(project(":db-tester-junit-spring-boot-starter"))
        api(project(":db-tester-spock-spring-boot-starter"))
        api(project(":db-tester-kotest-spring-boot-starter"))
    }
}

mavenPublishing {
    configure(com.vanniktech.maven.publish.JavaPlatform())

    pom {
        name = "DB Tester BOM"
        description = "Bill of Materials for DB Tester framework - manages versions for all modules"
    }
}
