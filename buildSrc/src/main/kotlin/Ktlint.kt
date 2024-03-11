import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

fun Project.setupKtlint() {
    plugins.apply("org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
        }
        filter {
            exclude {
                it.file.path.contains(layout.buildDirectory.dir("generated").get().toString())
            }
            //exclude(projectDir.toURI().relativize())
        }
    }
}
