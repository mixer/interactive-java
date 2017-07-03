# Mixer Interactive Java 2

A Java client for [Mixer's interactive 2 Protocol](https://dev.mixer.com/reference/interactive/protocol/protocol.pdf). 

For an introduction to interactive2 checkout the [reference docs](https://dev.mixer.com/reference/interactive/index.html) on the developers site.

## Documentation

To be completed

## Development

We use [Maven](http://maven.apache.org/) to build the client.  Once you have [Maven installed](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html), there are two easy steps to getting the
client in your classpath.

First, add the [Mixer repo](https://maven.mixer.com) to your `pom.xml` as a `<repository>` as follows:

```xml
<repositories>
  <repository>
    <id>mixer-snapshots</id>
    <url>https://maven.mixer.com/content/repositories/snapshots/</url>
  </repository>
</repositories>
```

And secondly, add this project as a `dependency` in your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.mixer</groupId>
    <artifactId>interactive-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

Once these steps are completed, you should have the client on your
classpath, and are set to get programming!

## Examples

To be completed

## Contributing

Thanks for your interested in contributing, checkout the [TODO](TODO.md) for a list of tasks!

Open a [Pull Request](https://github.com/WatchMixer/interactive-java/pulls), we'd love to see your contributions.