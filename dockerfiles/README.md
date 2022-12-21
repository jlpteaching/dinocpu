In the dinocpu folder, run the following command,

```sh
docker run --rm -u $(id -u $USER):$(id -g $USER) -v $PWD:/home/sbt-user -w /home/sbt-user -it <docker_container_tag>
```

If you're using Apptainer, you can run the following command,

```sh
apptainer run --bind $(pwd):/home/sbt-user --workdir /home/sbt-user docker://<docker_image>
```
