---
title: Adding Custom Libraries Into Local Leiningen Repository
tags: leiningen clojure maven
---

Sometimes, your project depends on a library which is not in
[clojars](http://clojars.org/), or maybe it is propriety library which
you can't upload to clojars. In this case, you can put it to your
local repository your self to solve the dependency.

     mvn install:install-file \
     -Dfile=mysql-connector-java-5.1.10-bin.jar \
     -DgroupId=self \
     -DartifactId=mysql-connector \
     -Dversion=5.1.10 \
     -Dpackaging=jar \
     -DgeneratePom=true

This will add the mysql adapter into your local Maven2 repository under
groupId self and artifactId mysql-connector, you can then edit your
project.clj, adding this dependency as,

    [self/mysql-connector "5.1.10"]
