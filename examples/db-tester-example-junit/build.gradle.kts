plugins {
    id("dbtester.example")
    java
}

description = "DB Tester Example - JUnit Jupiter examples and integration tests"

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-junit"))
                implementation(project(":db-tester-core"))
                implementation(platform(libs.junit.bom))
                implementation(libs.junit.jupiter)

                implementation(libs.derby.client)
                implementation(libs.derby.embedded)
                implementation(libs.derby.tools)
                implementation(libs.h2)
                implementation(libs.hsqldb)

                implementation(platform(libs.testcontainers.bom))
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.mssqlserver)
                implementation(libs.testcontainers.mysql)
                implementation(libs.testcontainers.neo4j)
                implementation(libs.testcontainers.oracle.free)
                implementation(libs.testcontainers.postgresql)

                implementation(libs.mssql.jdbc)
                implementation(libs.mysql.connector.j)
                implementation(libs.neo4j.jdbc.full.bundle)
                implementation(libs.oracle.ojdbc17)
                implementation(libs.postgresql)

                runtimeOnly(libs.datafaker)

                implementation(platform(libs.slf4j.bom))
                implementation(libs.slf4j.api)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}
