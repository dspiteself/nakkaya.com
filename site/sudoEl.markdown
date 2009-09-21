---
layout: default
title: sudoEl
---

This package contains some modified functions from
[SudoSave](http://www.emacswiki.org/cgi-bin/wiki/SudoSave). 
It will allow emacs to start processes as root.

##Installation

Download [dsniffMode](http://github.com/nakkaya/emacs/blob/master/int/sudo.el)

just place sudo.el in your load path.

## Usage

    (sudo-start-process "ls" (list "ls"))

#####Arguments
 - name of process/buffer.
 - command and arguments as list.

In this example ls will be run as root buffer will be named
*ls* and process name will be ls.

##### For bug reports/fixes/help See

[Contact](/contact.markdown)
