import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	id("org.flywaydb.flyway") version "9.22.1"
}

flyway.url="jdbc:postgresql://localhost:8081/finance-db"
flyway.user="admin"
flyway.password="admin"
flyway.locations = arrayOf("filesystem:db/migration", "filesystem:src/main/resources/db/migration")

group = "demo"
version = "0.1.0-Beta"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	// Базовые зависимости
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.postgresql:postgresql:42.6.0")

	// Для джавы, если её придётся использовать.
	compileOnly("org.projectlombok:lombok:1.18.26")

	// Валидация и безопасность
	implementation("org.springframework.security:spring-security-core:6.1.2")
	implementation("org.springframework.security:spring-security-crypto:6.1.2")
	implementation("commons-validator:commons-validator:1.7")

	// БД
	implementation("org.flywaydb:flyway-core:9.21.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
