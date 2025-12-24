plugins {
	application
	id("com.gradleup.shadow") version "9.3.0"
	id("org.openjfx.javafxplugin") version "0.1.0"
}

val ccVersion: String by extra

allprojects {
	group = "net.clgd"
	version = "1.1.0" + if (System.getenv("GITHUB_SHA") == null) {
		""
	} else {
		"-${System.getenv("GITHUB_SHA")}"
	}

	repositories {
		mavenCentral()

		exclusiveContent {
			forRepository {
				maven("https://maven.squiddev.cc")
			}
			filter {
				includeGroup("cc.tweaked")
			}
		}
	}

	plugins.withType<JavaBasePlugin> {
		java {
			toolchain {
				languageVersion = JavaLanguageVersion.of(17)
			}
		}

		tasks.withType(JavaCompile::class) {
			options.compilerArgs.addAll(listOf("-Xlint", "-Xlint:-processing"))
		}
	}
}

application {
	mainClass.set("net.clgd.ccemux.init.Launcher")
}

dependencies {
	implementation(project(":plugin-api"))

	runtimeOnly("org.slf4j:slf4j-simple:2.0.6")

	implementation("commons-cli:commons-cli:1.4")
	implementation("org.apache.commons:commons-lang3:3.6")

	implementation("com.google.code.gson:gson:2.8.1")

	compileOnly("com.google.auto.service:auto-service:1.0.1")
	annotationProcessor("com.google.auto.service:auto-service:1.0.1")

	testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

javafx {
	version = "19"
	modules = listOf("javafx.controls")
}

tasks.processResources {
	exclude("**/*.xcf") // GIMP images

	filesMatching("ccemux.version") {
		expand("version" to version, "cc_version" to ccVersion)
	}
}

tasks.jar {
	manifest {
		attributes(
			"SplashScreen-Image" to "img/splash2.gif",
			"Implementation-Version" to project.version,
		)
	}
}

tasks.shadowJar {
	archiveClassifier = "cct"
	description = "A shadowed jar which bundles all dependencies"

	minimize {
		exclude(dependency("org.slf4j:.*:.*"))
	}

	mergeServiceFiles()
	append("META-INF/LICENSE")
	append("META-INF/LICENSE.txt")
	append("META-INF/NOTICE")
	append("META-INF/NOTICE.txt")

	exclude(listOf(
		// Exclude random junk
		"META-INF/maven/*/*/*.*",
	))
}

tasks.assemble.configure {
	dependsOn(tasks.shadowJar)
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType(AbstractArchiveTask::class.java).configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	filePermissions {}
	dirPermissions {}
}
