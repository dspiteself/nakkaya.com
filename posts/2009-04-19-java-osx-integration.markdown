---
layout: post
title: Java & OS X Integration
---

Apple provides an Application class that allows you to integrate your
application with the OS X environment. It allows Java applications to
behave more like native OS X applications.

## Handling Quit

Following snippet will install an window listener and run your clean up
code before the application is exited. However when user selects Quit
instead of hitting the red dot clean up code will not run.


    addWindowListener(new WindowAdapter(){
           public void windowClosing(WindowEvent we){

           //do something here
	   System.exit(0);
	}});


In order to handle quit menu item. Apple's Application class class
provides handlers. But as a down side these classes are apple only
classes you should not load them when you are not OS X. So wrap them in
a class and load that class only when you are on OS X and use standard
window listener on other operating systems.


    import com.apple.eawt.*;
    import com.apple.mrj.*;

    public class MacApplication extends Application {

        public MacApplication( ) {

  		public void handleQuit( ApplicationEvent event ) {
                    // do something here...
		    System.exit(0);
		}

	    });
        }
    }


Now when Quit menu item is selected your application will run your clean
up code.

## Hiding your application

When you close a window on a mac, application is kept running and only the
window is hidden. In order for our application to play nice with native
applications we should also implement this. Fortunately Application class
provides a handler for that too.


    public void handleReOpenApplication(ApplicationEvent event) {
        //mainFrame.setVisible(true);
    }

Now when the dock icon is clicked your application will be visible and
when the red dot is clicked, it will only be hidden. Don't forget to set
your main frame's default close operation to hide.


## Knowing your OS

If you are not on OS X, you shouldn't load MacApplication class. On
mac's mrj.version system property is always set you can check it's value
to see if you are on a mac. If it's set create your application object.


	if(System.getProperty("mrj.version") == null){
	    addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent we){
                        //not on a mac cleanup
			System.exit(0);
		    }});
	}else{	    
	    MacApplication macApplication = new MacApplication(  );
	}


#####Resources
 - [Application (Apple Java Extensions)](http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/com/apple/eawt/Application.html)
