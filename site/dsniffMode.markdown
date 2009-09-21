---
layout: default
title: Emacs Dsniff Interface
---

an interface to dsniff.
 
#####Supported operating systems
 - Mac Os X 10.5 (Emacs 22.2)
 - Debian Lenny (Emacs 23)

#####Besides dsniff you need
 - sudo
 - sysctl
 - ifconfig
 - [sudoEl](/sudoEl.markdown)
 - [nmapMode](/nmapMode.markdown) (if you want to scan or import scans from nmap)
 - tcpdump (optional)

## Installation

Download [dsniffMode](http://github.com/nakkaya/emacs/blob/master/int/dsniff.el)

First you need to place dsniff.el in your load path. Then set following
options in your .emacs file.


    (setq dsniff-os "osx")
    (setq dsniff-network-interface "en1")
    (setq dsniff-arpspoof-program "/opt/local/sbin/arpspoof")
    (setq dsniff-urlsnarf-program "/opt/local/sbin/urlsnarf")
    (setq dsniff-dsniff-program "/opt/local/sbin/dsniff")
    (setq dsniff-msgsnarf-program "/opt/local/sbin/msgsnarf")
    (setq dsniff-mailsnarf-program "/opt/local/sbin/mailsnarf")
    (setq dsniff-tcpdump-program "/opt/local/sbin/tcpdump")

Thats it.

## Usage

First turn on ip forwarding. (Note it is an internal variable turn it
on from here not command line.) then select your gateway you can either
enter it manually or hit detect and set it automatically, you can either
set you host manually or import a scan from nmap.
(you need [nmapMode](/nmapMode.markdown)). 
Pick what you want to sniff and hit start.


##### For bug reports/fixes/help See

[Contact](/contact.markdown)
