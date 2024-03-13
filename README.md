# Introduction
The provided CLI tool generates a single Maven module into which we can easily add a REST services and a client as well as write tests. However, typically we have, multi module Maven project. Hence, created sample modules to validate following:
* Separate module for REST service and the REST client.
* Calling another service using the REST client.

# Multi module project

## Layout
This was easy to achieve with following modules:
* **service**: This module contains the REST service.
* **public**: This module contains the REST client and anything that needs to be shared.

### service module
This module contains the REST controller and uses the **[Micronaut Maven Plugin](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/index.html)** to build the fat jar.
Created following two services:
* **[user-service](user-service/service/src/main/java/org/expedientframework/user/UserServiceController.java)**: Returns the user name given a user id.
* **[hello-service](hello-service/service/src/main/java/org/expedientframework/hello/HelloServiceController.java)**: Calls the user-service to get the user name and then returns the greeting as "Hello userName".

### public module
This module contains the [declarative http client](https://micronaut-projects.github.io/micronaut-guides-poc/latest/micronaut-http-client-maven-java.html#declarative-client) using the **[@Client](https://docs.micronaut.io/2.1.4/api/io/micronaut/http/client/annotation/Client.html)** annotation.
```java
@Client(id = "userService", path = "/users")
public interface UserServiceClient {
    @Get("/{userId}")
    String getUser(long userId);
}
```
#### Defining http client configuration
As seen above, apart from the http Get method, the **[UserServiceClient](user-service/public/src/main/java/org/expedientframework/user/UserServiceClient.java)** has following two attributes for the **_@Client_** annotation:
* **_path_**: This is the relative path.
* **_id_**: This is the service ID to be used for looking up the configuration. The [configuration](user-service/public/src/main/resources/user-service-client.yml#L5) at minimum contains the http url for the service as shown below:
```yaml
micronaut:
  http:
    services:
      user-service:
        url: 'http://localhost:9090'
```
Note that the service ID changes from camel-case in **_@Client_** annotation to kebab-case in yaml file.

#### Making the http client available to the consumers
Consumers can use the http client by adding dependency on the **_public_** module. However, the configuration is not directly available to the consumer. Tried following options:
1. Copied the above configuration yaml properties into consumers **_application.yml_** file which is not user-friendly.
2. Used the **_[additional configuration files](https://docs.micronaut.io/1.3.0.M1/guide/index.html#_included_propertysource_loaders)_** option passing the consumer configuration file classpath as **_micronaut.config.files_** parameter value.
3. Read the additional configuration file from another module using classpath and manually passed the properties as property source during application start up as mentioned [here](https://docs.micronaut.io/1.3.0.M1/guide/index.html#propertySource).

With all of the above options, the consumer has an overhead of making sure the configuration is defined properly. Ideally, consumer should just add dependency on the **_public_** module and import the client to consume it. This was achieved by defining a [class implementing](user-service/public/src/main/java/org/expedientframework/user/UserServiceClientConfigurer.java) the **_[ApplicationContextConfigurer](https://docs.micronaut.io/3.2.1/api/io/micronaut/context/ApplicationContextConfigurer.html)_** interface and having the **_[@ContextConfigurer](https://docs.micronaut.io/3.5.3/api/io/micronaut/context/annotation/ContextConfigurer.html)_** annotation as below:
```java
@ContextConfigurer
public class UserServiceClientConfigurer implements ApplicationContextConfigurer {
    @Override
    public void configure(final ApplicationContextBuilder builder) {
        final Map<String, Object> properties = readPropertiesYamlFileAsMap();
        builder.properties(properties);
    }
}
```
That's it. When consumer service starts then above class is auto-detected and its **_configure()_** method is called.

# Micronaut AOT POC
Generated the above-mentioned http service client configurer using AOT as described in **[Micronaut AOT](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)**.
## Code generator class
Created **_[ServiceClientConfigurerSourceGenerator](common/core/src/main/java/org/expedientframework/common/ServiceClientConfigurerSourceGenerator.java)_** class implementing the **_io.micronaut.aot.core.AOTCodeGenerator_** interface.
* The ID for the code generator used is **_service.client.configurer.source.generator_**
* It expects comma-separated list of service client fully-qualified class names as **_service.client.classes_** property value.
* It generates a class implementing the **_[ApplicationContextConfigurer](https://docs.micronaut.io/3.2.1/api/io/micronaut/context/ApplicationContextConfigurer.html)_** interface which loads the required properties.
* Added **_org.expedientframework.common.ServiceClientConfigurerSourceGenerator_** as a service entry in [io.micronaut.aot.core.AOTCodeGenerator](common/core/src/main/resources/META-INF/services/io.micronaut.aot.core.AOTCodeGenerator). <span style="color:red">**_This step is not documented and without this the code generator will not be executed._**</span>
* Added **_[micronaut-aot-core](common/core/pom.xml#L22)_** Maven dependency. Without this dependency the build will fail with following error:
```text
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.10.1:compile (default-compile) on project core: Fatal error compiling: java.lang.IllegalArgumentException: The argument does not represent an annotation type: AOTModule -> [Help 1]
```

## Configuring the micronaut-maven-plugin
### pom.xml
Added following plugin configuration to **_[pom.xml](hello-service/service/pom.xml#L84)_**
```xml
<plugin>
    <groupId>io.micronaut.build</groupId>
    <artifactId>micronaut-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-service-client-sources</id>
            <goals>
                <goal>aot-analysis</goal>
            </goals>
            <phase>generate-sources</phase>
            <configuration>
                <enabled>true</enabled>
                <packageName>org.expedientframework.hello.aot</packageName>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.expedientframework.hello</groupId>
            <artifactId>public</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</plugin>
```
* **_goal:_** Used the **_aot-analysis_** goal of the plugin.
* **_phase:_** The default phase is **_package_** but used **_generate-sources_** since the generated class is needed for the tests to run successfully.
* **_configuration:_**
  * **_enabled:_** Enabled AOT analysis by setting this to **_true_**. With this setting the AOT code generator will be executed as part of Maven build itself.
  * **_packageName:_** Package name is required and the generated class will be put into this package.
* **_dependencies:_** Had to add dependency for the service client module since it is referenced in the generated source file.

### aot.properties
Added **_[aot.properties](hello-service/service/aot.properties)_** file at the root of the Maven module with following properties:
```properties
# Enable the code generator
service.client.configurer.source.generator.enabled = true
# Comma separated list of http service client class fully-qualified names
service.client.classes = org.expedientframework.hello.HelloServiceClient, org.expedientframework.user.UserServiceClient
```
### Output
* The generated files are under **_target/aot_** and are copied accordingly under **_target/classes_**.
* If you want to view the generated Java files then they are under **_target/aot/jit/generated/sources_**.

With above changes and configurations, was able to generate and consume service client configurer classes during the build.

# Observations
## Maven dependencies
For the top most module, the parent is defined as **_micronaut-parent_** which has all the required dependencies declared with required versions. 
```xml
<parent>
    <groupId>io.micronaut</groupId>
    <artifactId>micronaut-parent</artifactId>
    <version>3.9.2</version>
</parent>
```
It was observed that using latest version for any of the dependencies does not work. For example, if latest version of logback is used then logging stops working.
## AOT Code Generator
Tried to implement **_AOTCodeGenerator_** using following steps mentioned in **[Micronaut AOT](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)**.
* Defined custom class **_org.expedientframework.user.MyResourceGenerator_** implementing the **_AOTCodeGenerator_** interface with ID as **_my.resource.generator_**.
* Added **_aot.properties_** at the root of the Maven module which has property to enable the code generator as below:
```properties
my.resource.generator.enabled = true
greeter.message = Hello, world!
```
However, the code generator class was not getting invoked when maven plugin was run using following command:

```shell
mvn package -Dmicronaut.aot.enabled=true -Dmicronaut.aot.packageName=com.ajeydudhe
```
After looking into Micronaut sources and some trial & error, figured out that we need to add the code generator class as service entry under **_META-INF/services/io.micronaut.aot.core.AOTCodeGenerator_**.
<br/>
For example, added file **_user-service/service/src/main/resources/META-INF/services/io.micronaut.aot.core.AOTCodeGenerator_** with below fully-qualified class name for the custom code generator:
```yaml
org.expedientframework.user.MyResourceGenerator
```
With above, the custom code generator was getting invoked during compilation.
