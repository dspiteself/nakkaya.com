---
title: Unit Testing in Clojure
tags: clojure unit-testing
---

One thing I love about Clojure is the built in unit tests. Unit tests
are great for making sure your code does what it needs to do and
introducing new features or bug fixes doesn't break anything. You can
refactor your code anytime you want and be sure that you did not break
anything. Unit tests also serve as a living documentation for your code
base, newcomers can look at the code base and get basic understanding of
how your API works.


Clojure's core library includes a test framework written by Stuart
Sierra. If there is anything that is not covered here the best place to
look for it is the [source
code](http://github.com/richhickey/clojure/blob/master/src/clj/clojure/test.clj)
itself.


#### Defining Tests

Testing framework is under the namespace clojure.test,

    (use 'clojure.test)

is all thats needed to load the framework. Assuming we would like to
test the following function,

    (defn add2 [x] 
      (+ x 2)) 

There are two ways to define tests, you can either define your tests
with the function itself,

    (with-test
     (defn add2 [x] 
      (+ x 2))
     (is (= 4 (add2 2)))
     (is (= 5 (add2 3))))

but I believe that just bloats the code base, or you can define your
tests separately using the deftest macro,

    (deftest test-adder
      (is (= 24  (add2 22))))

Tests can also be grouped together,

    (deftest arithmetic
      (addition)
      (subtraction))

#### Running Tests

To run the tests you defined from REPL you can use,

    (run-tests)

With out a namespace run-tests will run the tests defined in the
namespace you are in, you can pass it namespaces to run tests defined in
other namespaces as well.

    (run-tests 'your.namespace 'some.other.namespace)

If you want to run all tests in all namespaces,

    (run-all-tests)

can be used.

#### Ant Integration

If you call your tests from an ant target, build completes successfully
even if tests fail, from clojure source code I cannibalized some
functions to use in my build process. Ant task that we will use to call
our tests look like this,

    <target name="test" depends="">
      <java classname="clojure.main" 
	    fork="true" failonerror="true">  
        <classpath>
          <pathelement path="${test-dir}" />
          <pathelement path="${src-dir}" />
          <pathelement location="${extLibs.dir}/clojure.jar"/>
          <pathelement location="${extLibs.dir}/clojure-contrib.jar"/>
        </classpath>

        <arg value="-e" />
        <arg value="
		    (use 'clojure.test)
		    (use 'app-test)
		    (run-ant)" />
      </java>  
    </target>

Now create a file in test-dir called app_test.clj that will be the main
entry point in your application for tests. In it put the following
definitions [from clojure
source](http://github.com/richhickey/clojure/blob/abca86ea023080fd4ceed24b9887a653a56722eb/test/clojure/test_clojure.clj).

    (def test-names
         [:app-test])
 
    (def test-namespaces
         (map #(symbol (str (name %)))
              test-names))
 
    (defn run
      "Runs all defined tests"
      []
      (println "Loading tests...")
      (apply require :reload-all test-namespaces)
      (apply run-tests test-namespaces))

Runs all defined tests, prints report to \*err\*, throw if failures. This
works well for running in an ant java task.

    (defn run-ant []
      (let [rpt report]
        (binding [;; binding to *err* because, in ant, when the test target
                  ;; runs after compile-clojure, *out* doesn't print anything
                  *out* *err*
                  *test-out* *err*
                  report (fn report [m]
                             (if (= :summary (:type m))
                               (do (rpt m)
                                   (if (or (pos? (:fail m)) 
                                           (pos? (:error m)))
                                     (throw 
                                      (new Exception (str (:fail m) 
                                                          " failures, " 
                                                          (:error m) 
                                                          " errors.")))))
                               (rpt m)))]
          (run))))

Add namespaces you wish to test to test-names. Now when one tests fails,
ant build process will fail also.
