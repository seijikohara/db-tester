import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("com.vanniktech.maven.publish")
}

extensions.configure<MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    pom {
        url = "https://github.com/seijikohara/db-tester"
        inceptionYear = "2025"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "seijikohara"
                name = "Seiji Kohara"
                email = "seiji.kohara@gmail.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/seijikohara/db-tester.git"
            developerConnection = "scm:git:ssh://github.com/seijikohara/db-tester.git"
            url = "https://github.com/seijikohara/db-tester"
        }
    }
}
