# Updating the DINO CPU code

If you're not a git expert, I suggest you check out the great [Pro Git book](https://git-scm.com/book/en/v2).
However, the code below should get you started.
**Caution**: I have not tested the code below.
I wrote this off the top of my head.
You can always create a copy of your git repo before beginning!

![ If that doesn't fix it, git.txt contains the phone number of a friend of mine who understands git. Just wait through a few minutes of 'It's really pretty simple, just think of branches as...' and eventually you'll learn the commands that will fix everything.](https://imgs.xkcd.com/comics/git.png)
https://xkcd.com/1597/

If you don't want to use your code from lab 1, but want to use my code to get started run the following git commands in the dinocpu/ directory:

```
git checkout -b my-lab1-solution
git add .
git commit -m "Add my solution to lab 1"
git checkout main
git pull
```

The commands above create a new branch (my-lab1-solution) and then commit all of your current outstanding changes to that branch.
Then, it checks out the master branch and pulls the updates from the dinocpu repository (on jlpteaching).

If you have already committed things to master you can use the following:

```
git checkout -b my-lab1-solution
git checkout main
git reset --hard origin/main
git pull
```

This creates a new branch (my-lab1-solution) then resets your master branch to be the same as the origin's master branch.
Finally, it pulls any updates from the origin (jlpteaching/dinocpu, presumably).

If you want to use your own solution to lab 1 it's a little more complicated:

```
git add .
git commit -m "Add my lab1 solution"
git fetch
git merge origin/main
```

This will merge the updates in origin (jlpteaching/dinocpu) into your master branch.
**There will be merge conflicts** since both you and I modified cpu.scala and alucontrol.scala.
You will have to decide how to deal with those conflicts.
See https://help.github.com/articles/resolving-a-merge-conflict-using-the-command-line/ for more information.
You could also do the same as the first two options to create a my-lab1-solution branch and set master to track origin/master directly then merge master with my-lab1-solution.

There are many other ways to deal with this in git. These are the simplest that I could come up with off the top of my head. I'm happy to help out more in office hours if you get stuck. However, debugging git over piazza is going to be tough :). We'll do our best, though!
