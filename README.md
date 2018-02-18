Bean Mirror
=========

[![Apache 2 License](https://img.shields.io/badge/license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/Elopteryx/bean-mirror.svg?branch=master)](https://travis-ci.org/Elopteryx/bean-mirror)
[![Coverage Status](https://coveralls.io/repos/github/Elopteryx/bean-mirror/badge.svg?branch=master)](https://coveralls.io/github/Elopteryx/bean-mirror?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.elopteryx/bean-mirror/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.elopteryx/bean-mirror)
[![JavaDoc](https://img.shields.io/badge/javadoc-n/a-brightgreen.svg)](http://www.javadoc.io/doc/com.github.elopteryx/bean-mirror)

Bean Mirror is a fluent, type-safe reflection library for the latest Java versions.

It is not released yet.

Features
--------
* Fluent API
* Type-safe
* Compact
* Modularized

Requirements
--------
* Java 10 (it will be changed to the next LTS version (11) when it comes out)

Motivation
--------

There are several reflection based libraries which allow the developers to easily manipulate objects
during runtime. What separates this library is that it uses the newer API (java.lang.invoke) which allows
faster code if used properly, compared to the classic java.lang.reflect package.
Because several additions have been added in Java 9 setting the minimum required Java version to anything
lower is not feasible (it would not make sense to create another library, which would not be any different
from the existing ones). On the other hand the Java release schedule has been changed and the next version
which will receive long-term-support is 11, therefore the plan is to make that version the base line.