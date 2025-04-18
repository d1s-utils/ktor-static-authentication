/*
 * Copyright 2022-2024 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("com.github.ben-manes.versions")
}

buildscript {
    dependencies {
        val dokkaVersion: String by project

        classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    }
}

val projectGroup: String by project
val projectVersion: String by project

group = projectGroup
version = projectVersion

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion: String by project

    api("io.ktor:ktor-server-auth:$ktorVersion")
}

publishing {
    repositories {
        maven {
            name = "mavenD1sDevRepository"

            val channel = if (isDevVersion) {
                "snapshots"
            } else {
                "releases"
            }

            url = uri("https://maven.d1s.dev/${channel.toLowerCaseAsciiOnly()}")

            credentials {
                username = System.getenv("MAVEN_D1S_DEV_USERNAME")
                password = System.getenv("MAVEN_D1S_DEV_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            if (isDevVersion) {
                val commitShortSha = System.getenv("GIT_SHORT_COMMIT_SHA")

                commitShortSha?.let {
                    version = "$version-$it"
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<DokkaTask> {
    dokkaSourceSets {
        configureEach {
            val moduleDocsPath: String by project

            includes.setFrom(moduleDocsPath)
        }
    }

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "Copyright (c) 2022-2023 Mikhail Titov"
    }
}

kotlin {
    explicitApi()
}

val isDevVersion get() = version.toString().endsWith("-dev")