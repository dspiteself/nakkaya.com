---
title: Multiple Terminals in Emacs
tags: emacs
---


[Emacs](http://www.gnu.org/software/emacs/) provides term-mode, 
which is terminal emulation for emacs. Since
term-mode is real terminal emulation it allows you to run any console
application you want (even ncurses based ones). But it does not provide
a easy way to switch between them if you have multiple buffers open.

    (defun na-switch-between-terminals () 
    "cycle multiple terminals"
    (interactive)
    (if (not (eq (or (get-buffer "*terminal*") 
                     (get-buffer "*inferior-lisp*"))  nil ) )
        (progn     
          (setq found nil)
          (bury-buffer)
          (setq head (car (buffer-list)))      
          (while  (eq found nil)        
            (set-buffer head)   
            (if (or (eq major-mode 'term-mode ) 
                    (eq major-mode 'inferior-lisp-mode ))
                (setq found t )
              (progn
               (bury-buffer)
               (setq head (car (buffer-list)))))))))

Using this snippet you can cycle between multiple terms in a circular
fashion.
