---
title: Using Multiple Lisps with Inferior Lisp
tags: emacs
---

I was reading [On Lisp](http://www.paulgraham.com/onlisp.html) which
uses [Common Lisp](http://en.wikipedia.org/wiki/Common_Lisp) through out
the book, so I needed a quick way to switch between lisps, following is
a quick hack to switch between different lisp programs. When you call
na-run-lisp without any prefix it will run the first item in
lisp-programs, when called with a prefix you can select which lisp to
run.

     (setq lisp-programs 
           (list (list "clojure" clojure-command)
                 (list "sbcl" "/opt/local/bin/sbcl")))

     (defun na-run-lisp (arg)
       (interactive "P")
       (if (null arg)
           (run-lisp (second (first lisp-programs)))
         (let (choice) 
           (setq choice (completing-read "Lisp: " (mapcar 'first lisp-programs)))
           (dolist (l lisp-programs)
             (if (string= (first l) choice)
                 (run-lisp (second l)))))))
