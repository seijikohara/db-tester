plugins {
    id("dbtester.example")
    groovy
}

description = "DB Tester Example - Spock examples and integration tests"

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-spock"))
                implementation(project(":db-tester-core"))
                implementation(platform(libs.groovy.bom))
                implementation(libs.groovy)
                implementation(libs.groovy.sql)

                implementation(libs.derby.client)
                implementation(libs.derby.embedded)
                implementation(libs.derby.tools)
                implementation(libs.h2)
                implementation(libs.hsqldb)

                implementation(platform(libs.testcontainers.bom))
                implementation(libs.testcontainers.mssqlserver)
                implementation(libs.testcontainers.mysql)
                implementation(libs.testcontainers.neo4j)
                implementation(libs.testcontainers.oracle.free)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.testcontainers.spock)

                implementation(libs.mssql.jdbc)
                implementation(libs.mysql.connector.j)
                implementation(libs.neo4j.jdbc.full.bundle)
                implementation(libs.oracle.ojdbc17)
                implementation(libs.postgresql)

                implementation(platform(libs.slf4j.bom))
                implementation(libs.slf4j.api)
                runtimeOnly(libs.slf4j.simple)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}
