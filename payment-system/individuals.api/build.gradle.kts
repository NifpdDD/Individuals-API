import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


val versions = mapOf(
	"keycloakAdminClientVersion" to "22.0.3",
	"springdocOpenapiStarterWebfluxUiVersion" to "2.5.0",
	"mapstructVersion" to "1.5.5.Final",
	"javaxAnnotationApiVersion" to "1.3.2",
	"javaxValidationApiVersion" to "2.0.0.Final",
	"javaxServletApiVersion" to "2.5",
	"logbackClassicVersion" to "1.5.18",
	"nettyResolverVersion" to "4.1.121.Final:osx-aarch_64",
	"feignMicrometerVersion" to "13.6",
	"testContainersVersion" to "1.19.3"
)

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
		mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
	}
}

plugins {
	java
	idea
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.13.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Individuals API"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(24))
	}
}

repositories {
	mavenCentral()
}


/*
──────────────────────────────────────────────────────
============== Api generation ==============
──────────────────────────────────────────────────────
*/
val openApiDir = file("${rootDir}/openapi")

val foundSpecifications = openApiDir.listFiles { f -> f.extension in listOf("yaml", "yml") } ?: emptyArray()
logger.lifecycle("Found ${foundSpecifications.size} specifications: " + foundSpecifications.joinToString { it.name })

foundSpecifications.forEach { specFile ->
	val ourDir = getAbsolutePath(specFile.nameWithoutExtension)
	val packageName = defineJavaPackageName(specFile.nameWithoutExtension)

	val taskName = buildGenerateApiTaskName(specFile.nameWithoutExtension)
	logger.lifecycle("Register task ${taskName} from ${ourDir.get()}")
	val basePackage = "individuals.api.${packageName}"

	tasks.register<GenerateTask>(taskName) {
		generatorName.set("spring")
		inputSpec.set(specFile.absolutePath)
		outputDir.set(ourDir)

		configOptions.set(
			mapOf(
				"library" to "spring-cloud",
				"useBeanValidation" to "true",
				"useSpringBoot3" to "true",
				"openApiNullable" to "false",
				"apiPackage" to "${basePackage}.api",
				"modelPackage" to "${basePackage}.dto",
				"configPackage" to "${basePackage}.config"
			)
		)

		doFirst {
			logger.lifecycle("$taskName: starting generation from ${specFile.name}")
		}
	}
}


fun getAbsolutePath(nameWithoutExtension: String): Provider<String> {
	return layout.buildDirectory
		.dir("generated-sources/openapi/${nameWithoutExtension}")
		.map { it.asFile.absolutePath }
}

fun defineJavaPackageName(name: String): String {
	val beforeDash = name.substringBefore('-')
	val match = Regex("^[a-z]+]").find(beforeDash)
	return match?.value ?: beforeDash.lowercase()
}

fun buildGenerateApiTaskName(name: String): String {
	return buildTaskName("generate", name)
}

fun buildJarTaskName(name: String): String {
	return buildTaskName("jar", name)
}

fun buildTaskName(taskPrefix: String, name: String): String {
	val prepareName = name
		.split(Regex("[^A-Za-z0-9]"))
		.filter { it.isNotBlank() }
		.joinToString("") { it.replaceFirstChar(Char::uppercase) }

	return "${taskPrefix}-${prepareName}"
}

val withoutExtensionNames = foundSpecifications.map { it.nameWithoutExtension }

sourceSets.named("main") {
	withoutExtensionNames.forEach { name ->
		java.srcDir(layout.buildDirectory.dir("generated-sources/openapi/$name/src/main/java"))
	}
}

tasks.register("generateAllOpenApi") {
	foundSpecifications.forEach { specFile ->
		dependsOn(buildGenerateApiTaskName(specFile.nameWithoutExtension))
	}
	doLast {
		logger.lifecycle("generateAllOpenApi: all specifications has been generated")
	}
}

tasks.named("compileJava") {
	dependsOn("generateAllOpenApi")
}



dependencies {

	implementation ("com.github.ben-manes.caffeine:caffeine:3.2.3")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.keycloak:keycloak-admin-client:${versions["keycloakAdminClientVersion"]}")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${versions["springdocOpenapiStarterWebfluxUiVersion"]}")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

	compileOnly("org.projectlombok:lombok")
	compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstructVersion"]}")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testImplementation("org.testcontainers:testcontainers:${versions["testContainersVersion"]}")
	testImplementation("org.testcontainers:postgresql:${versions["testContainersVersion"]}")
	testImplementation("org.testcontainers:junit-jupiter:${versions["testContainersVersion"]}")
	testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-15")

	implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

	implementation("javax.validation:validation-api:${versions["javaxValidationApiVersion"]}")
	implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApiVersion"]}")

//	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
//	implementation("io.micrometer:micrometer-observation")
//	implementation("io.micrometer:micrometer-tracing")
//	implementation("io.micrometer:micrometer-tracing-bridge-otel")
//	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
//	implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

	implementation("io.netty:netty-resolver-dns-native-macos:${versions["nettyResolverVersion"]}")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}


tasks.named<Test>("test") {
	useJUnitPlatform()
}