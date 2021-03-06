#### Disclaimer
This is still a work in progress! This repository is only
created for educational purposes (specifically, for my 
masters thesis).


### Introduction

This repository is created to provide a distributed in-memory
database solution written in Java that uses the [Bizur consensus algorithm](https://arxiv.org/pdf/1702.04242.pdf).

The paper can be found in directory ```./docs/bizur_paper.pdf```.


### Description

The repository is divided into 4 main branches.

* **master:** Contains the java implementation of the database.
* **examples:** Contains some basic examples of how to use the database.
* **repository:** Contains the binaries of the built java libraries including mpi and jbizur (this project).
* **benchmark:** A separate project that is used to compare the performance of different in-memory database solutions with the bizur db.


### Where to Start

Please see the ```examples``` branch to see the basic usage 
of this distributed database.

To run the examples you need:
* Java 8
* Maven

Checkout the ```examples``` branch and in the main dir 
compile with command:
```
$ mvn clean install
```
Import the code using your favourite IDE and use it as you
wish.


### Notes and TODO

* See the [issues](https://github.com/mboysan/jbizur/issues)
section for more details on what will be implemented next.
* **NB!** Since the code is constantly evolving, comments 
will be added to the implementation details once the 
project is in finalization state.

