---
title: Git Delete Last Commit
tags: git
---

Once in a while late at night when i ran out of coffee, i commit stuff
that i shouldn't have. Then i spend the next 10 - 15 minutes googling
how to remove the last commit i made. So after third time i wanted to
make a record of it so i can refer to it later.


If you have committed junk but not pushed,

    git reset --hard HEAD~1

HEAD~1 is a shorthand for the commit before head. Alternatively you can
refer to the SHA-1 of the hash you want to reset to. Note that when
using --hard any changes to tracked files in the working tree since
the commit before head are lost.


Now if you already pushed and someone pulled which is usually my case,
you can't use git reset. You can however do a git revert,

    git revert HEAD

This will create a new commit that reverses everything introduced by the
accidental commit.
