---
layout: default
title: Compiling Java apps that use MRJ on a non-Apple computer
---

I use multiple OS's for development mostly OS X and Linux.  If you
create an application that plays well with OS X. It will work with Linux
, Mac or windows but you can't compile it under a non Apple machine
because it uses non standard apple libraries (com.apple) not available under Linux
or Windows. To overcome this problem we can use the Java Reflections
API. Following these tips [Java & OS X Integration](/2009/04/19/java-osx-integration/) you can stick all your
Mac specific code under a single class and you can load it using
reflections api. when you do that compiler will not compile about
missing libraries since you are not accessing any Mac specific classes
until runtime, as long as you are not trying to compile that Mac
specific class.


    try{
        Class klass = Class.forName("macOs.MacApplication");
		
        Class[] paramTypes = {
        	String.class,
	        String.class };
        Constructor cons = klass.getConstructor(paramTypes);
    
        Object[] args = {
        	"test",
	        "this" };
    
        Object theObject = cons.newInstance(args);
    
    }catch( Exception e ) { 
    }

#####References
 - [Apple Java Extensions](http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/overview-summary.html)
