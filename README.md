# Apache Fineract CN default setup

This project provides resources which can be used to create a default setup for microservices in [Apache Fineract CN](https://github.com/search?q=org%3Aapache+fineract) deployment. It is dependent on the apis and importers of those microservices.

## Abstract
Apache Fineract CN is an application framework for digital financial services, a system to support nationwide and cross-national financial transactions and help to level and speed the creation of an inclusive, interconnected digital economy for every nation in the world.

## Steps needed to create a default setup for a new microservice 

1.  Create a module with the same name as the microservice.  Add it to settings.gradle, and build.gradle of the composite build.

2.  Place the csv files in subfolder of main/resources
    
3.  Change build.gradle of your module to reference the importer of your microservice in compileTest 

4.  Write a test which checks the format of your csv files to prevent data corruption.  See accounting/ImportTest for an example of how to do this.

## Versioning
The version numbers follow the [Semantic Versioning](http://semver.org/) scheme.

In addition to MAJOR.MINOR.PATCH the following postfixes are used to indicate the development state.

* BUILD-SNAPSHOT - A release currently in development. 
* M - A _milestone_ release include specific sets of functions and are released as soon as the functionality is complete.
* RC - A _release candidate_ is a version with potential to be a final product, considered _code complete_.
* RELEASE - _General availability_ indicates that this release is the best available version and is recommended for all usage.

The versioning layout is {MAJOR}.{MINOR}.{PATCH}-{INDICATOR}[.{PATCH}]. Only milestones and release candidates can  have patch versions. Some examples:

1.2.3-BUILD-SNAPSHOT  
1.3.5-M.1  
1.5.7-RC.2  
2.0.0-RELEASE

## License
See [LICENSE](LICENSE) file.
