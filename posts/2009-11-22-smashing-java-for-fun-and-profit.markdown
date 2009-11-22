---
title: Smashing Java for Fun and Profit
tags: java
---

Every Java programming forum or mailing list, i am subscribed to has
hundreds of people asking the same question, "How can i prevent people
from reversing my application?". Short answer is you can not prevent
reverse engineering regardless of the programming language used,
unfortunately Java/C# just makes the process a lot more easier. This
post will go over the process of cracking a very simple Java
application.

We begin with a very simple, "Hello, World!" application. Given a
correct serial number, which is "1234" it will print "Hello World!" to
the console, for any other serial it will exit without a prompt.

    public class hello {
    
        public static boolean checkSerial(String serial){
	    if (serial.equals("1234"))
	        return true;
	    else
	        return false;
        }

        public static void main (String args[]) {
	
	    if (args.length != 1){
	        System.err.println("Serial Needed..");
	        return;
	    }

	    if (checkSerial(args[0]) == false )
	        System.exit(0);

	    System.out.println("Hello World!");
        }
    }

Corresponding ant file to build a jar file for the project,

    <project name="hello" default="def" basedir=".">

      <target name="def">
        <javac srcdir="." includes="hello.java" fork="yes"/>

        <jar destfile="hello.jar" > 
          <manifest>
            <attribute name="Main-Class" value="hello"/>
          </manifest>
          <fileset dir=".">
            <include name="hello.class"/>
          </fileset>
        </jar>
      </target>

    </project>

Type,

    ant

to build the application, it will create a hello.jar in the same
directory.


Everyone says it is very easy to decompile Java applications, but
actually how easy it is? My favorite tool for this job is the
[JD-GUI](http://java.decompiler.free.fr/?q=jdgui). Try opening the jar
file we produced.

![JD-GUI](/images/post/jd-reverse.png)

As you can see, for this application you do not need to do anything to
crack it, serial is written in plain text. For demonstration purposes,
assume that the checkSerial function is a proper algorithm to check if a
serial is valid or not. 

Now a cracker has two options at this point, he can learn the algorithm
that checkSerial uses and create a serial that will pass the inspection,
or we can patch the checkSerial function to return true no matter, what
serial is passed.

I'll go with the second route, for this we'll use a library called
[Javassist](http://www.csg.is.titech.ac.jp/~chiba/javassist/), which
allows you to manipulate bytecode.

Jar files are glorified zip files, so we can extract the content of the
jar file using,

    unzip hello.jar

Using the javassist we write a small snippet, that will read the
bytecode rename the checkSerial function to something else then create a
new function that will always return true and add that. Finally we write
back the modified class file.

    import javassist.*;
 
    class smash{
        public static void main(String[] argv) throws Exception{

	    //Load the class that we will be patching...
	    ClassPool pool = ClassPool.getDefault();
	    CtClass klass = pool.get("hello");
 
	    //Get the method we want to patch, and rename...
	    CtMethod orig = klass.getDeclaredMethod("checkSerial");
	    orig.setName( "checkSerial$impl" );
 
	    // Create a new function that will always return true...
	    CtMethod patch = CtNewMethod.copy(orig, "checkSerial", klass, null);
	    patch.setBody("{ return true; }");
 
	    // Add patched method..
	    klass.addMethod( patch );
	    klass.writeFile();
 
	    System.out.println("Done Patching.");

	    CtMethod[] methods = klass.getDeclaredMethods();
	    for( int i=0; i<methods.length ; i++){
	        System.out.println( "\t" + methods[i].getLongName() );
	    }

        }
    }

Compile this file,

    javac -cp .:javassist.jar smash.java 

Run it in the same directory containing the .class file,

    java -cp .:javassist.jar smash

You should see a output similar to,

    Done Patching.
            hello.checkSerial$impl(java.lang.String)
            hello.main(java.lang.String[])
            hello.checkSerial(java.lang.String)

Put back the patched .class file in to the jar file,

    $ zip hello.jar hello.class

Now we can run the application passing any serial we want, and it will
work.

    $ java -jar hello.jar 34345345
    Hello World!

Lets move to the other end of the spectrum, What can be done to
prevent this attack? 

Well, not much as long as the application runs on the hostile territory
(user), it can be reversed and patched. You can however make reversing
process harder, by obfuscating your class files.

Bytecode obfuscators, protects your class files by replacing package,
class, method, and field names with inexpressive characters. Some
bytecode obfuscators do more than just name mangling such as scrambling
your code flow in a way that makes it really hard to follow.

In my experience the obfuscator that causes the minimal amount of hassle
is [yGuard](http://www.yworks.com/en/products_yguard_about.htm).

        <!-- yGuard Ant task. -->
        <taskdef name="yguard" 
                 classname="com.yworks.yguard.YGuardTask" 
                 classpath="yguard.jar"/>
        <!-- Integrated obfuscation and name adjustment... -->
        <yguard>
          <inoutpair in="./hello.jar" out="./hello-final.jar"/>
      
          <rename logfile="./test.log" replaceClassNameStrings="true">
	    <property name="obfuscation-prefix" value="name"/>
	    <keep>
	      <class name="hello"/>
	      <method name="void main(java.lang.String[])" 
		      class="hello" />
	    </keep>
          </rename>
        </yguard>

We add the yGuard task to our build process, we keep the main class
intact not to break the jar file, if we open the resulting
jar file "hello-final.jar" in the JD-GUI,

![JD-GUI](/images/post/yguard.png)

We see that the method name has been changed to A, well for this simple
example it is still trivial to figure out what is going on but in a code
base composed of 100's of class files, it becomes pretty hard to figure
out what is going on.


There are other schemes in the tubes, such as encrypting the class files
then load them with a custom class loader, well the problem people don't
get is if you want it to run on a CPU, it has to be decoded at some
point and can be reversed.
