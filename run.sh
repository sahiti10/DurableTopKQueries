#!/bin/bash
mkdir -p out
javac -d out src/durabletopk/*.java
java -cp out durabletopk.ExperimentalRunner