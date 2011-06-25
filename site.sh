# #!/bin/sh

IFS=$'\n'

if [ $# -eq 0 ]; then
    echo "Deploy or View.."
    exit
fi

if [ $1 = "view" ] 
then
    echo "Building..."
    java -jar static-1.0.0-SNAPSHOT-standalone.jar -b
    java -jar static-1.0.0-SNAPSHOT-standalone.jar -j
fi

if [ $1 = "deploy" ] 
then
    echo "Deploying..."

    git add .
    git commit -a -m 'gh-pages push'
    git push origin master

    rm -rf /tmp/site/
    java -jar static-1.0.0-SNAPSHOT-standalone.jar -b

    rm -rf /tmp/site/
    mv site/ /tmp/
    mv static-1.0.0-SNAPSHOT-standalone.jar /tmp/

    git checkout gh-pages
    git rm -r .
    git clean -fdx

    cp -r /tmp/site/ .
    rm -rf /tmp/site/

    git add .
    git commit -a -m 'gh-pages push' 
    git push origin gh-pages

    git checkout master
    mv /tmp/static-1.0.0-SNAPSHOT-standalone.jar ./
fi
