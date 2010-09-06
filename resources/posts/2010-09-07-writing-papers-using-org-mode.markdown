---
title: Writing Papers Using org-mode
tags: org-mode
---

With [LaTeX](http://en.wikipedia.org/wiki/LaTeX), it seems like I am
spending more time researching LaTeX itself then the topic I am working
on, so for sometime now I dropped LaTeX in favor of
[Org-mode](http://orgmode.org/). I am posting this as a personal cheat
sheet, it is much more easier to search this site then to dig through
old papers to figure out how I did stuff.

Org-mode allows you to control certain things during export using
[export
options](http://www.gnu.org/software/emacs/manual/html_node/org/Export-options.html),
following sets the default font to *Arial*,

    #+LATEX_HEADER: \renewcommand{\rmdefault}{phv} % Arial

and gets rid of the red boxes drawn around the links.

    #+LATEX_HEADER: \usepackage{hyperref}
    #+LATEX_HEADER: \hypersetup{
    #+LATEX_HEADER:     colorlinks,%
    #+LATEX_HEADER:     citecolor=black,%
    #+LATEX_HEADER:     filecolor=black,%
    #+LATEX_HEADER:     linkcolor=blue,%
    #+LATEX_HEADER:     urlcolor=black
    #+LATEX_HEADER: }


For those who write in non English,
[babel](http://en.wikibooks.org/wiki/LaTeX/Internationalization) package
will translate, automatically generated text strings to the language you 
specify.

    #+LATEX_HEADER: \usepackage[turkish]{babel}

For specifying date, author, and title of the paper you are writing,

    #+TITLE: Writing Papers Using org-mode
    #+AUTHOR: Nurullah Akkaya
    #+EMAIL: nurullah@nakkaya.com

You can't have papers without figures, images,

    #+CAPTION: Arduino Duemilanove
    #+ATTR_LaTeX: scale=0.75
    [[./img/arduino-duemilanove.jpeg]]

or pre-formatted text,

     #+BEGIN_EXAMPLE
     Some example from a text file.
     #+END_EXAMPLE

for embedding source,

    #+BEGIN_SRC lisp
    (let [i (atom 0)]
      (defn generate-unique-id
        "Returns a distinct numeric ID for each call."
        []
        (swap! i inc)))
    #+END_SRC

If you want to listing to look like the fontified Emacs buffer you need
to add these to your *.emacs*,

    (require 'org-latex)
    (setq org-export-latex-listings t)
    (add-to-list 'org-export-latex-packages-alist '("" "listings"))
    (add-to-list 'org-export-latex-packages-alist '("" "color"))
