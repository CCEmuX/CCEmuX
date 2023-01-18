plugins {
	application
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

val ccVersion = project.property("cc_version")

allprojects {
	group = "net.clgd"
	version = "1.0.0" + if (System.getenv("GITHUB_SHA") == null) {
		""
	} else {
		"-${System.getenv("GITHUB_SHA")}"
	}

	repositories {
		mavenCentral()

		maven("https://squiddev.cc/maven/") {
			name = "cc-tweaked"
		}
	}

	gradle.projectsEvaluated {
		java {
			sourceCompatibility = JavaVersion.VERSION_1_8
			targetCompatibility = JavaVersion.VERSION_1_8
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

	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")
	implementation("org.apache.logging.log4j:log4j-core:2.11.2")

	implementation("commons-cli:commons-cli:1.4")
	implementation("org.apache.commons:commons-lang3:3.6")
	implementation("io.netty:netty-codec-http:4.1.85.Final")
	implementation("it.unimi.dsi:fastutil:8.3.0")
	implementation("org.ow2.asm:asm:8.0.1")

	implementation("com.google.code.gson:gson:2.8.1")

	compileOnly("com.google.auto.service:auto-service:1.0-rc6")
	annotationProcessor("com.google.auto.service:auto-service:1.0-rc6")

	testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
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
	archiveClassifier.set("cct")
	description = "A shadowed jar which bundles all dependencies"

	minimize {
		exclude(dependency("org.slf4j:.*:.*"))
		exclude(dependency("org.apache.logging.log4j:.*:.*"))
	}

	append("META-INF/LICENSE")
	append("META-INF/LICENSE.txt")
	append("META-INF/NOTICE")
	append("META-INF/NOTICE.txt")

	exclude(listOf(
		// Exclude random junk
		"META-INF/maven/*/*/*.*",
		// Exclude textures and JSON from the CC jar
		"assets/computercraft/**/*.json",
		"data/computercraft/**/*.json",
		"assets/computercraft/textures/block/*",
		"assets/computercraft/textures/item/*",
	))
}

tasks.assemble.configure {
	dependsOn(tasks.shadowJar)
}

tasks.test {
	useJUnitPlatform()
}
