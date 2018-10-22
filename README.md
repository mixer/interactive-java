# Mixer Interactive Java 2 [![Build Status](https://travis-ci.org/mixer/interactive-java.svg?branch=master)](https://travis-ci.org/mixer/interactive-java)

The Java Interactivity SDK supports client-side development of application with Mixer Interactivity. The SDK implements the [Mixer Interactive 2 protocol](https://dev.mixer.com/reference/interactive/protocol/protocol.pdf) specification.

For an introduction to Interactive 2.0, checkout the [reference docs](https://dev.mixer.com/reference/interactive/index.html) on the developers site.

## Release Notes

Please refer to the [release notes](https://github.com/mixer/interactive-java/releases) for more information.

## Documentation

Javadoc for the SDK is available [here](https://mixer.github.io/interactive-java/).

## Development

### Maven

We use [Maven](http://maven.apache.org/) to build the client.  Once you have [Maven installed](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html), there are two easy steps to getting the
client in your classpath.

First add the [Mixer repo](https://maven.mixer.com) as a `<repository>` in your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>mixer-releases</id>
    <url>https://maven.mixer.com/content/repositories/releases/</url>
  </repository>
  <repository>
    <id>mixer-snapshots</id>
    <url>https://maven.mixer.com/content/repositories/snapshots/</url>
  </repository>
</repositories>
```

Next, add this project as a `dependency` in your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.mixer</groupId>
    <artifactId>mixer-interactive-api</artifactId>
    <version>3.2.0</version>
  </dependency>
</dependencies>
```

Once these steps are completed, you should have the client on your classpath, and are set to get programming!

### Gradle

You can also use [Gradle](https://gradle.org/) to build the client. Once you have [installed Gradle](https://gradle.org/install/), there are two things you have to add to your `build.gradle` file.

First add the [Mixer repo](https://maven.mixer.com) as a `repository` in your `build.gradle`:

```groovy
repositories {
  maven {
    url 'https://maven.mixer.com'
  }
}
```

Next, add this project as a `dependency` in your `build.gradle` using the `implementation` configuration:
```groovy
dependencies {
  implementation 'com.mixer:mixer-interactive-api:3.2.0'
}
```

Once these steps are completed, you should have the client on your classpath, and are set to get programming!

## Contributing

Is there a feature missing that you'd like to see, or have you found a bug that you have a fix for? Do you have an idea or just interest in helping out in building the library? Let us know and we'd love to work with you. For a good starting point on where we are headed and feature ideas, take a look at our [contributing](CONTRIBUTING.md) guide.

Big or small we'd like to take your contributions to help improve the Mixer Interactivity API for everyone.
