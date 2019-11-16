# Testing for grading

## How to do it

If you run the test under the "Grader" config, you can run just the grading scripts.
This assumes that you are running inside the gradescope docker container.

```
sbt "Grader / testOnly dinocpu.test.grader.LabXGrader"
```

See [run_autograder](run_autograder) for more details.
This file must be updated for each lab.
It may be a good idea to create a different file for each lab and update the Dockerfile to copy the correct one into the Docker image.

### Updating the docker image

```
docker build -f Dockerfile.gradescope -t jlpteaching/dino-grading:labX .
```

Make sure that you have checked out the labX branch before building the docker file so you only include the template code and not the answer.

To test the grade image, run

```
docker run --rm -w $PWD -v $PWD:$PWD -it jlpteaching/dinocpu-grading:labX bash
```

In the dinocpu directory, check out the master branch (which has the correct solution).

Then, create the following directories in the container image.

```
mkdir /autograder/submission
mkdir /autograder/results
```

Copy the correct files into `/autograder/submission`.
Note, this varies for each lab.

Then, `cd` to `/autograder` and run `./run_autograder`.
This should produce a `results.json` filr in `/autograder/results` and print to the screen that all tests passed.
