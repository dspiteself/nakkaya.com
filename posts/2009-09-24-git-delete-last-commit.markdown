---
title: Git Delete Last Commit
tags: git
---

Once in a while late at night when I ran out of coffee, I commit stuff
that I shouldn't have. Then I spend the next 10 - 15 minutes googling
how to remove the last commit I made. So after third time I wanted to
make a record of it so I can refer to it later.

If you have committed junk but not pushed,

    git reset --hard HEAD~1

HEAD~1 is a shorthand for the commit before head. Alternatively you can
refer to the SHA-1 of the hash you want to reset to. Note that when
using --hard any changes to tracked files in the working tree since
the commit before head are lost.

> If you don't want to wipe out the work you have done, you can use
> *--soft* option that will delete the commit but it will leave all your
> changed files "Changes to be committed", as git status would put it.

Now if you already pushed and someone pulled which is usually my case,
you can't use git reset. You can however do a git revert,

    git revert HEAD

This will create a new commit that reverses everything introduced by the
accidental commit.
