![](src/res/icon.png)
# Meeting Helper
Distance learning is new for everyone, and the last thing you want to happen is to miss class or be tardy.  
However, don't worry, that's why there's MeetingHelper.  
Based on your schedule, MeetingHelper will open your class meeting link in your browser automatically so that you don't have to!  
To get started, click the link below, download the latest version, and follow the setup instructions below.  
Download Link: https://github.com/jonathanjma/MeetingHelper/releases/latest

## Features
+ Automatic meeting link opening (runs in background)
+ Today view to see your schedule and alerts for that day
+ Tray icon to open GUI/exit
+ Customizable Options:
    + Class Schedule
    + Class Meeting Links
    + Alternate Meeting Links (if a class has different links for different days)
    + How Early to Open Meeting Link
    + General Reminder Alerts
+ See [images](#images) below

## Setup Instructions
1. If you __have Java 8__ installed, download MeetingHelper.zip from the [releases page](https://github.com/jonathanjma/MeetingHelper/releases/latest)
    + This was tested using Java 8, but any Java version > 8 should work
    + If you __don't__ have Java installed, download MeetingHelperBundled.zip (adds java libraries)
        + Or download and install the appropriate [Java 8 JRE](https://www.oracle.com/java/technologies/javase-jre8-downloads.html) for your computer
    + Another note- the .exe file will only run on Windows as MacOS and Linux do not support it
2. Locate the downloaded .zip file and extract all the files ("Extract All" button in file explorer)
3. Once extracted, run the MeetingHelper.exe file
    + If you get a warning from Windows that this is an unrecognized app, click "Run Anyways" (might be under "More Info")
    + A window should open with a default bell schedule
    + If you downloaded MeetingHelperBundled.zip, you must keep the __"jre"__ folder in the __same__ folder as the .exe file
4. In the options window, set your options. The correct format for each option is below:
    + Schedule: \<period#> \<startTime> (ex. 1 8:00)
    + Links: put in period order, link must include "https://"
    + Alternate Links: \<period#> \<day of week> \<link> (ex. 6 Monday https://zoom.us/test)
    + Alerts: \<day of week> \<time> "\<alertText>" (ex. Friday 14:30 "School Over")
    + Make sure you are using 24-hour time
    + If you see any error notifications, make sure the format for each option is correct
5. Make sure all fields marked with a * have been filled out
6. Click the "Save Options" button (might have to scroll down)
7. If you click on the "Today" tab, you should see your schedule and alerts for today
    + You can also manually open meeting links by clicking the buttons below the "All Links" header or by clicking the button next to each period
8. That's it!
    + To open or exit the app, click the tray icon (the one with a clock on it) at the bottom right of your screen next to the system icons (battery, Wi-Fi, etc)
    + Clicking once opens a menu while double clicking opens the today view by default
    + Enjoy :-)

## Version History

#### v3.1
+ Tray icon and other minor bug fixes

#### v3.0
+ Added today and about view
+ Can manually open meeting links
+ Can check for updates
+ Reduced memory usage

#### v2.0
+ GUI to change options
+ Can use custom schedule
+ Can change how early to open meeting links
+ Added support for alternate links
+ Added support for reminder alerts

#### v1.0
+ Meeting links automatically opened
+ Notifications when meeting links opened
+ Follows Mission San Jose HS bell schedule
+ Set meeting links in text file
+ Tray Icon

## Images
![](imgs/today.png)
![](imgs/options.png)
![](imgs/about.png)
![](imgs/tray.png)