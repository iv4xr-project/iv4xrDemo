# Cucumber

This Cucumber extension has been implemented following a similar structure to [cucumber-junit5-example](https://github.com/cronn/cucumber-junit5-example)  

├──  `src\test\`  
│   ├── `src\test\java`  
│   │  
│   │   ├── `src\test\java\bdd\RunAllCucumberTests.java` JUnit Engine to indicate both the IDE and Gradle where to find the Cucumber tests.  
│   │   ├── `src\test\java\bdd\state` Within the scenarios, we share state object and variables between steps.  
│   │   ├──`src\test\java\bdd\steps` Within the scenarios, we associate Gherkin step definitions with Java functions.  
│   │  
│   ├── `src\test\resources`  
│   │   ├──`src\test\resources\features` The folder that contains the Cucumber feature files with the LabRecruits test scenarios.  
│  
└──  

## Cucumber dependencies

To integrate Cucumber into the project, it is necessary to include JUnit and Cucumber Maven dependencies in the `pom.xml` file.  

- The `io.cucumber` versions `X.Y.Z` must coincide. But not necessary with the JUnit engine version.  

- The `<scope>test</scope>` variable indicates the Cucumber tests should be implemented under the `src\test` project package. You can remove to also implement Cucumber classes in `src\main`.  

[cucumber-java](https://mvnrepository.com/artifact/io.cucumber/cucumber-java)

```
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

[cucumber-junit-platform-engine](https://mvnrepository.com/artifact/io.cucumber/cucumber-junit-platform-engine)

```
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>X.Y.Z</version>
	<scope>test</scope>
</dependency>
```

[junit-platform-suite](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-suite)

```
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <version>A.B.C</version>
    <scope>test</scope>
</dependency>
```

## Cucumber profile

Moreover, we have create a cucumber profile that allows to execute all cucumber tests from command line. It invokes the `RunAllCucumberTests.java` runner class which, by default, executes all Cucumber features from the `bdd.steps` package.  

```
<profiles>
    <profile>
        <id>cucumber</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.22.2</version>
                    <configuration>
                        <includes>
                            <include>**/RunAllCucumberTests*.java</include>
                        </includes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Maven Execution

### Configure LabRecruits paths

Set the LabRecruits path of your system in all the necessary `feature` files.  

Update the local path of this step: **Given the path 'C:/Users/username/labrecruits/path'**

```
Feature: Load a LabRecruits level

  Scenario: LabRecruits level can be loaded and monster observed
    Given the path 'C:/Users/username/labrecruits/path'
    Given the level 'simple_enemy_bdd'
    Given the graphics 'true'
    When the game starts
    Then the agent 'agent0' observes the entity 'orc1'
```

### Command Line execution

These commands clean the project and execute all Cucumber feature tests that are included in the cucumber profile.  

`mvn clean`  
`mvn test -P cucumber`  

## Cucumber plugin for Eclipse

Eclipse should automatically detect the existence of `feature` files and recommend the installation of the [cucumber-eclipse-plugin](https://marketplace.eclipse.org/content/cucumber-eclipse-plugin)

### Eclipse Execution

1. Select `src\test\java\bdd\RunAllCucumberTests.java`
2. Run as...
3. JUnit (5) tests