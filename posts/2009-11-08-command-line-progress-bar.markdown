---
title: Command Line Progress Bar
tags: clojure c++ java
---

I frequently need a progress bar for applications, in order to visualize
what is going on in the application. Following is a simple progress bar
implemented in three different languages, C++, Clojure and Java.

    [================>                                 ]   33%

They all look the same, just call the appropriate function with the
percentage to show.

#### C++

    void printProgBar( int percent ){
      std::string bar;

      for(int i = 0; i < 50; i++){
        if( i < (percent/2)){
          bar.replace(i,1,"=");
        }else if( i == (percent/2)){
          bar.replace(i,1,">");
        }else{
          bar.replace(i,1," ");
        }
      }

      std::cout<< "\r" "[" << bar << "] ";
      std::cout.width( 3 );
      std::cout<< percent << "%     " << std::flush;
    }

#### Clojure

    (defn print-progress-bar [percent]
      (let [bar (StringBuilder. "[")] 
        (doseq [i (range 50)]
          (cond (< i (int (/ percent 2))) (.append bar "=")
                (= i (int (/ percent 2))) (.append bar ">")
                :else (.append bar " ")))
        (.append bar (str "] " percent "%     "))
        (print "\r" (.toString bar))
        (flush)))

#### Java

        public static void printProgBar(int percent){
            StringBuilder bar = new StringBuilder("[");

            for(int i = 0; i < 50; i++){
                if( i < (percent/2)){
                    bar.append("=");
                }else if( i == (percent/2)){
                    bar.append(">");
                }else{
                    bar.append(" ");
                }
            }
        
            bar.append("]   " + percent + "%     ");
            System.out.print("\r" + bar.toString());
        }
