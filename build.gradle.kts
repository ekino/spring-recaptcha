import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "1.3.71"
  `java-library`
  `maven-publish`
  signing

  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  id("com.ekino.oss.plugin.kotlin-quality") version "1.0.0"
  id("org.jetbrains.dokka") version "0.10.0"
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
  mavenCentral()
  jcenter()
  mavenLocal()
}

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:${property("spring-boot.version")}"))

  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("io.github.microutils:kotlin-logging:${property("kotlin-logging.version")}")

  implementation("com.squareup.retrofit2:retrofit:${property("retrofit.version")}")
  implementation("com.squareup.retrofit2:converter-jackson:${property("retrofit.version")}")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${property("spring-boot.version")}")
  kapt("org.springframework.boot:spring-boot-configuration-processor:${property("spring-boot.version")}")

  testImplementation("com.willowtreeapps.assertk:assertk-jvm:${property("assertk-jvm.version")}")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.ninja-squad:springmockk:${property("springmockk.version")}")
  testImplementation("com.github.tomakehurst:wiremock:${property("wiremock.version")}")
  testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:${property("spring-cloud-contract-wiremock.version")}")
}

configurations {
  all {
    exclude(module = "junit")
    exclude(module = "mockito-core")
  }
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allJava)
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("dokka")
  archiveClassifier.set("javadoc")
  from(buildDir.resolve("dokka"))
}

tasks {
  withType<Test> {
    useJUnitPlatform()
    jvmArgs(
      "-Dspring.test.constructor.autowire.mode=ALL",
      "-Djunit.jupiter.testinstance.lifecycle.default=per_class"
    )
  }

  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }

  withType<Sign> {
    onlyIf {
      !gradle.taskGraph.hasTask(":publishToMavenLocal")
    }
  }

  register("printVersion") {
    doLast {
      val version: String by project
      println(version)
    }
  }

  artifacts {
    archives(jar)
    archives(sourcesJar)
    archives(javadocJar)
  }
}

val publicationName = "mavenJava"

publishing {
  publications {
    register<MavenPublication>(publicationName) {
      pom {
        name.set("spring-recaptcha")
        description.set("spring-recaptcha provide a custom filter to validate reCaptcha.")
        url.set("https://github.com/ekino/spring-recaptcha")
        licenses {
          license {
            name.set("MIT License (MIT)")
            url.set("https://opensource.org/licenses/mit-license")
          }
        }
        developers {
          developer {
            name.set("Hervé Lascaux")
            email.set("herve.lascaux@ekino.com")
            organization.set("ekino")
            organizationUrl.set("https://www.ekino.com/")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/ekino/spring-recaptcha.git")
          developerConnection.set("scm:git:ssh://github.com:ekino/spring-recaptcha.git")
          url.set("https://github.com/ekino/spring-recaptcha")
        }
        organization {
          name.set("ekino")
          url.set("https://www.ekino.com/")
        }
      }
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
      from(components["java"])
    }

    repositories {
      maven {
        val ossrhUrl: String? by project
        val ossrhUsername: String? by project
        val ossrhPassword: String? by project

        url = uri(ossrhUrl ?: "")

        credentials {
          username = ossrhUsername
          password = ossrhPassword
        }
      }
    }
  }
}

signing {
  sign(publishing.publications[publicationName])
}
