plugins {
    id("dbtester.java-common")
}

pluginManager.withPlugin("java") {
    tasks.withType<Test>().configureEach {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
