---
Author: Jason Lowe-Power
Title: DINO CPU Overview
---

# DINO CPU

The DINO (Davis IN Order) CPU is a simple RISC-V CPU for use in the [ECS 154B Computer Architecture class](https://github.com/jlpteaching/ECS154B) as University of California, Davis.

The DINO CPU is currently in an early Beta.
We started using it in the Winter 2019 offering of 154B and we forsee making significant updates for future quarters.

## Working with the DINO CPU code

You can find all of the details on how to work with the DINO CPU in the [README file](https://github.com/jlpteaching/dinocpu/blob/master/README.md) and the [documentation](https://github.com/jlpteaching/dinocpu/tree/master/documentation) in the [DINO CPU repository](https://github.com/jlpteaching/dinocpu/).

You must first download the template code before you can start working on the DINO CPU.
The template code is on GitHub, so you can simply clone the repository:

```
git clone https://github.com/jlpteaching/dinocpu.git
```

Then, if you change into the newly created `dinocpu` directory, you can run the Singularity container with Chisel and start working on the assignment!

```
cd dinocpu
singularity run library://jlowepower/default/dinocpu
[Lots of output your first time.
This may also take 5-10 minutes to download all of the libraries, etc.]
...
sbt:dinocpu>
```

More info on setting up the Sigularity container system can be found in the [Singularity documentation](singularity.md).
