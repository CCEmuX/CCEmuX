plugins {
	`java-library`
}

val ccVersion = project.property("cc_version")

dependencies {
	api("org.squiddev:cc-tweaked-1.16.5:$ccVersion")
	api("org.slf4j:slf4j-api:1.7.25")
	api("com.google.guava:guava:22.0")
}
