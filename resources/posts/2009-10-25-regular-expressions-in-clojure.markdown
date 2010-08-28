---
title: Regular Expressions in Clojure
layout: post
tags: clojure
---

Regular expression is a [formal
language](http://en.wikipedia.org/wiki/Formal_language#Programming_languages)
that will allow you to find chunks of text that matches the patterns you
specify. Following are a bunch of code examples which I've put together
as a mini-reference for my own use.


In clojure you can define a regular expression using,

    #"<patter>"

syntax.


To search for match in a string, you can use re-find, it will return
either the match or a vector of matches if you have groups.

    user=> (re-find #"quick" "The quick brown fox jumps over the lazy dog")
    "quick"

    user=> (re-find #"(f(oo bar))" "foo bar")
    ["foo bar" "foo bar" "oo bar"]

Like other data structures you can threat regex's as sequences too, re-seq
will return a lazy sequence of matches.

    user=> (re-seq #"h" "The quick brown fox jumps over the lazy dog")
    ("h" "h")

If you are coming from java world one thing that will confuse you is that
at first there seems to be no way to specify pattern flags such as,

    DOTALL 
    MULTILINE 
    UNICODE_CASE 

Instead of those flags you can use embedded flags.

 - Unix lines mode can enabled via  (?d).
 - Case-insensitive mode can enabled via  (?i).
 - Multiline mode can enabled via (?m).
 - Dotall mode can enabled via (?s).
 - Unicode-aware case folding can enabled via (?u).
