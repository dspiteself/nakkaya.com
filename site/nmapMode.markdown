---
title: Emacs Nmap Interface
tags: nmap emacs
description: Emacs Nmap Interface
---


an interface to nmap port scanner.

#### Features

 - Runs asynchronously so it doesn't block your session
 - Allows multiple scans simultaneously.

#### Supported operating systems

 - Mac Os X 10.5 (Emacs 22.2)
 - Debian Lenny (Emacs 23)

#### Besides nmap you need

 - sudo
 - ifconfig
 - [sudoEl](/sudoEl.markdown)

#### Installation

Download [nmapMode](http://github.com/nakkaya/emacs/blob/master/int/nmap.el)

First you need to place nmap.el in your load path. Then set following
options in your .emacs file.

     (load "nmap.el")
     (setq nmap-nmap-program "/usr/bin/nmap")
     (setq nmap-ifconfig-program "/sbin/ifconfig")
     (setq nmap-network-interface "ath0")

Thats it.

#### Usage

After installation type M-x nmap to load. If you ip or ip block reads
0.0.0.0 you may have trouble with your ifconfig path or interface.

Pressing Host Discovery or h in the buffer will scan the subnet for
hosts and will print the list. you can click on any ip to initiate a
port scan.

If you want to scan a single target press t or click on the target ip
you will be prompted for target. Pressing s or clicking Scan target will
initiate a scan.

#### Keyboard Shortcuts
 - c - Clear Buffer
 - t - Set Target
 - h - Host Scan
 - s - Scan Target(s)
 - q - Quit

For bug reports/fixes/help See [Contact](/contact.markdown)
