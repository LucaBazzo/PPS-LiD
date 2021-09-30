# PPS-LiD (Lost in Dungeons)

This project is aimed to develop a rogue-lite game based on the famous DeadCells.

You will play the Hero, who will be inside dungeons, with random maps and enemies to face.
You will try to advance and become stronger and stronger, but the truth is only one: there is no way out, and you will remain forever, lost in dungeons.

### Continuous Integration
Status of compilation and testing after each push.
- __master__ branch: 
  
  ![Continuous Integration](https://github.com/LucaBazzo/PPS-LiD/workflows/Scala%20CI/badge.svg?branch=master)

- __develop__ branch:  
  ![Continuous Integration](https://github.com/LucaBazzo/PPS-LiD/workflows/Scala%20CI/badge.svg?branch=develop)


## Build
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/LucaBazzo/PPS-LiD/Scala%20CI)


The system uses Sbt as build system and dependency manager.

To build:
```
$ git clone https://github.com/LucaBazzo/PPS-LiD.git
$ cd PPS-LiD
$ sbt compile
```

To test:
```
$ sbt test
```

To execute:
```
$ sbt run
```

## Usage
```
$ git clone https://github.com/LucaBazzo/PPS-LiD.git
$ cd PPS-LiD
$ sbt assembly
```

To launch the jar file:
```
$ java -jar target/scala-2.13/pps-lid-assembly-1.0.jar
```

## Authors

- Luca Bazzocchi - luca.bazzocchi2@unibo.it
- Giacomo Casadei - giacomo.casadei12@unibo.it
- Fabio Muratori - fabio.muratori2@unibo.it
- Luca Bracchi - luca.bracchi3@unibo.it