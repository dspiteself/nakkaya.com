---
layout: default
title: StartUpItem API
---

StartUpItem class, lets you modify Mac OS X startup items from java
application. StartUpItem can read modify and write startup items. It is
written entirely in java but uses "/bin/bash" and "defaults"
applications to do the actual modification.

#####with StartUpItem you can
 - read and parse startup items list under Mac OS X.
 - remove a startup item from list.
 - add a startup item.
 - save modifications.

#####Supported operating systems
 - Mac OS X 10.5.5

## Usage

Download [StartUpItem](http://gist.github.com/190989)

### Reading the list

	StartUpItem startUpItem = new StartUpItem();
	startUpItem.read();


After creating a StartUpItem object, make a call to read. Object will
read and parse current startup items. After the list is parsed you have
two functions to manipulate the list add and remove.

### To add an Item

To add a new item make a call to add. Add takes two parameters. First
parameter is path to application and second parameter is whether to hide
the application after startup or not. Pass true if you want your
the application hidden after startup, false otherwise.


 	startUpItem.add("/Applications/Firefox.app" , false);
 	startUpItem.add("/Applications/Adium.app" , true);
	startUpItem.add("/Applications/Chess.app" , true);
	startUpItem.add("/Applications/TextEdit.app" , false);
	startUpItem.add("/Applications/Preview.app" , false);
	startUpItem.add("/Applications/Mail.app" , false);
	startUpItem.add("/Applications/iTunes.app" , false);


### To remove an item

To remove a startup item make a call to remove passing application path
as a parameter.

	startUpItem.remove("/Applications/Firefox.app");
 	startUpItem.remove("/Applications/Chess.app");
	startUpItem.remove("/Applications/Preview.app");

### Saving Changes

There is no way to remove individual item from the list using
defaults. So what StartUpItem class does is removes whole startup items
from the list and writes all the list back again. You do not need to
remove the list your self, just make a call to write() list will deleted
and written back.


 	startUpItem.write();


## Known Bugs

For some reason defaults did not like Emacs.app even though it is
visible from command line, it gets removed as soon as Accounts
preferences is open.

##### For bug reports/fixes/help See

[Contact](/contact.markdown)
