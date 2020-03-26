# Codice NSILI 

## Codice NSILI Features 

Federated Source and Endpoint implementations of **STANAG 4559** to allow interoperability with other systems that implement the NSILI specification.

- [catalog-nsili-sourcestoquery-ui](https://github.com/codice/NSILI/blob/master/catalog-nsili-sourcestoquery-ui/README.md): Admin Console Plugin for configuring sources to query from NSILI endpoint.

## Building
### What you need ###
* [Install J2SE 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* Make sure that your JAVA\_HOME environment variable is set to the newly installed JDK location, and that your PATH includes %JAVA\_HOME%\bin (Windows) or $JAVA\_HOME$/bin (\*NIX).
* [Install Maven 3.1.0 \(or later\)](http://maven.apache.org/download.html). Make sure that your PATH includes the MVN\_HOME/bin directory.
* Set the MAVEN_OPTS variable with the appropriate memory settings
### Optional 
* If you do not wish to run formatting from the commandline (see below) you may use an IDE to format the code for you with the google-java-format plugins.
  - https://github.com/google/google-java-format
    * IntelliJ: https://plugins.jetbrains.com/plugin/8527
    * Eclipse: https://github.com/google/google-java-format/releases/download/google-java-format-1.3/google-java-format-eclipse-plugin-1.3.0.jar


### How to build ###
In order to run through a full build, be sure to have a clone for the [](ddf) repository and optionally the [](ddf-support) repository (NOTE: daily snapshots are deployed so downloading and building each repo may not be necessary since those artifacts will be retrieved.):

```
git clone git://github.com/codice/NSILI.git
```
Change to the root directory of the cloned ddf repository. Run the following command:

```
mvn install
```

