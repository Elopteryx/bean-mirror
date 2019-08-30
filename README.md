Bean Mirror
=========

[![Apache 2 License](https://img.shields.io/badge/license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Actions Status](https://github.com/Elopteryx/bean-mirror/workflows/build/badge.svg)](https://github.com/Elopteryx/bean-mirror/actions)
[![Coverage Status](https://coveralls.io/repos/github/Elopteryx/bean-mirror/badge.svg?branch=master)](https://coveralls.io/github/Elopteryx/bean-mirror?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.elopteryx/bean-mirror/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.elopteryx/bean-mirror)
[![JavaDoc](https://img.shields.io/badge/javadoc-1.1.0-brightgreen.svg)](http://www.javadoc.io/doc/com.github.elopteryx/bean-mirror)

Bean Mirror is a fluent, type-safe reflection library for the latest Java versions.

Features
--------
* Fluent API
* Type-safe
* Modularized
* Lightweight, no dependencies

Requirements
--------
* Java 11+

Motivation
--------

There are several reflection based libraries which allow the developers to easily manipulate objects
during runtime. What separates this library is that it uses the newer API (java.lang.invoke) which allows
faster code if used properly, compared to the classic java.lang.reflect package.

Because several additions have been added in Java 9 setting the minimum required Java version to anything
lower is not feasible (it would not make sense to create another library, which would not be any different
from the existing ones).

The project was inspired by [jOOR](https://github.com/jOOQ/jOOR). If you are using an older Java I suggest
using that instead.

Examples
--------

```java

        // Get the name of the school principal by field access
        final String principalName = BeanMirror.of(school, MethodHandles.lookup())
                .field("principal", Principal.class)
                .get("name", String.class);

```

```java

        // Create a setter function to set the value on any instance
        final BiConsumer<Student, Integer> setter = BeanMirror.of(Student.class, MethodHandles.lookup())
                .createSetter("startingYear", Integer.class);

        setter.accept(student, 2018);
        setter.accept(otherStudent, 2020);

```

Documentation
-------------

[Javadoc][1]

Gradle
-----
```xml
compile 'com.github.elopteryx:bean-mirror:1.1.0'
```

Maven
-----
```xml
<dependency>
    <groupId>com.github.elopteryx</groupId>
    <artifactId>bean-mirror</artifactId>
    <version>1.1.0</version>
</dependency>
```

Find available versions on [Maven Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.elopteryx%22%20AND%20a%3A%22bean-mirror%22).

[1]: http://www.javadoc.io/doc/com.github.elopteryx/bean-mirror/1.1.0