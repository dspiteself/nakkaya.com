---
title: Simple Bash Installer
tags: bash installer
---

Following is a simple bash script to build a installer for any *nix
system.

    #!/bin/bash

    SKIP=`awk '/^__DATA_BEGIN__/ { print NR +1; exit 0; }'  \$0`
    tail -n +\$SKIP \$0 | tar x
    exit 0;

    __DATA_BEGIN__

Save the script as "intaller.sh". 

Put everything you need to install in a folder and tar the folder,

    tar -cvvf foo.tar foo/

Concatenate everything into a file,

    cat installer.sh foo.tar > setup

Make it executable,

    chmod 755 setup

Now when you run setup it will extract the folder in to current
directory.
