import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	idea
	id("org.springframework.boot") version "4.0.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.13.0"
	id("maven-publish")
}

// Очистили мапу от дубликатов и неиспользуемых переменных логирования
val versions = mapOf(
	"mapstructVersion" to "1.5.5.Final",
	"springdocOpenapiStarterWebmvcUiVersion" to "2.5.0",
	"feignMicrometerVersion" to "13.6",
	"springCloudStarterOpenfeign" to "4.1.1",
	"testContainersVersion" to "1.19.3",
	"shardingsphereVersion" to "5.5.1"
)

group = "com.example"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

dependencyManagement {
	imports {
		// Рекомендуется проверять совместимость Spring Cloud под релиз Spring Boot 4
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.2")
		mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
	}
}

repositories {
	mavenCentral()
}

configurations.all {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
	//SECURITY
	implementation ("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.12.5")

	// SPRING CORE & MVC
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// OPENAPI / SWAGGER
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdocOpenapiStarterWebmvcUiVersion"]}")

	// REPOSITORIES & DATA METRICS
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign:${versions["springCloudStarterOpenfeign"]}")

	// OBSERVABILITY (Отрегулировано под Spring Boot 4 OTLP спецификации)
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")

	// PERSISTENCE & SHARDING
	implementation("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.apache.shardingsphere:shardingsphere-jdbc:${versions["shardingsphereVersion"]}")

	// CODE GENERATION HELPERS (Lombok + MapStruct)
	compileOnly("org.projectlombok:lombok")
	compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstructVersion"]}")

	// TEST SUITE
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers:${versions["testContainersVersion"]}")
	testImplementation("org.testcontainers:postgresql:${versions["testContainersVersion"]}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
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
	val basePackage = "com.example.${packageName}"

	tasks.register(taskName, GenerateTask::class) {
		generatorName.set("spring")
		inputSpec.set(specFile.absolutePath)
		outputDir.set(ourDir)

		typeMappings.set(
			mapOf(
				"InitTransactionRequest" to "InitTransactionRequest"
			)
		)
		
		importMappings.set(
			mapOf(
				"InitTransactionRequest" to "com.example.transaction.dto.InitTransactionRequest"
			)
		)

		configOptions.set(
			mapOf(
				"library" to "spring-cloud",
				"skipDefaultInterface" to "true",
				"useBeanValidation" to "true",
				"openApiNullable" to "false",
				"useFeignClientUrl" to "true",
				"useTags" to "true",
				"apiPackage" to "${basePackage}.api",
				"modelPackage" to "${basePackage}.dto",
				"configPackage" to "${basePackage}.config",
				"useJakartaEe" to "true",
				"jacksonWithJsonProvider" to "true"
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

/*
──────────────────────────────────────────────────────
============== Building jars ==============
──────────────────────────────────────────────────────
*/

tasks.named("build") {
	dependsOn(generatedJars)
}

val generatedJars = foundSpecifications.map { specFile ->
	val name = specFile.nameWithoutExtension
	val generateTaskName = buildGenerateApiTaskName(name)
	val jarTaskName = buildJarTaskName(name)
	val outDirProvider = getAbsolutePath(name)
	val generateSrcDir = outDirProvider.map { File(it).resolve("src/main/java") }

	val sourcesSetName = name

	val sourceSet = sourceSets.create(sourcesSetName) {
		java.srcDir(generateSrcDir)
		compileClasspath += sourceSets["main"].compileClasspath
	}

	val compileTaskName = "compile${sourcesSetName.replaceFirstChar(Char::uppercase)}Java"
	tasks.register<JavaCompile>(compileTaskName) {
		source = sourceSet.java
		classpath = sourceSet.compileClasspath
		destinationDirectory.set(layout.buildDirectory.dir("classes/${sourcesSetName}"))
		dependsOn(generateTaskName)
	}

	tasks.register<Jar>(jarTaskName) {
		group = "build"
		archiveBaseName.set(name)
		destinationDirectory.set(layout.buildDirectory.dir("libs"))

		val classOutput = layout.buildDirectory.dir("classes/${sourcesSetName}")
		from(classOutput)
		dependsOn(compileTaskName)

		doFirst {
			println("Building JAR for $name from compiled classes in ${classOutput.get().asFile}")
		}
	}
}
