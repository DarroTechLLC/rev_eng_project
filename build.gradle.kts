plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.darro_tech"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Mail dependencies
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.1.2")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // PDF Generation
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Environment Variables
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Duplicate handling strategy
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
        resources {
            srcDirs("src/test/resources")
        }
    }
} 
