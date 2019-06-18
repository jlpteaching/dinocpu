# To distribute template code for students

1. Check out the branch for the lab you want to distribute
2. Delete the history: `rm -rf .git`
3. Initialize the git repo: `git init`
4. Add all of the files: `git add .`
5. Make the commit: `git commit -m "Initial commit of template code"`
6. Add the template repo (e.g., `git remote add origin git@github.com:jlpteaching/dinocpu`)
7. Push the code: `git push -f origin master`

## To update the template code

1. Make changes in private repo to the labX branch. Note, you will probably want to make changes to the master branch then merge the labX branch.
2. Copy all changes over to the template repo: `cp -ru dinoprivate/* .`
3. Commit the changes to the template repo: `git commit`. You probably should comment on the git changeset hash is from the private repo in the commit message.
4. Push the code: `git push -f origin master`

**Note:** *If you know of a better way to do this, I'm all ears.*
