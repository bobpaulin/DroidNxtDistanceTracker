DroidNxtDistanceTracker
=======================

##Android Environment Setup - Scratch


Prerequisite: Install Java, Git


1. Download ADT Bundle for your system
2. Add Android 4.4.2 (API 19) SDK Platform, ARM EABI v7a System Image, Sources for Android SDK
3. Open Eclipse
4. Check out Git project https://github.com/cjug/DroidNxtDistanceTracker.git
5. Add appcompat v7 project to DroidNxtDistanceTracker project
6. Place phone in Development Mode
7. Run DroidNxtDistanceTracker as an Android App
8. Select you phone or emulator to see app (Note bluetooth won't work with emulator)

##Android Environment Setup - Assisted with Thumbdrive


Prerequisite: Install Java


1. Copy adt-bundle (mac or windows) to your PC
2. Copy appcompat_v7.zip and DroidNxtDistanceTracker.zip
3. Open Eclipse
4. Import Existing Project (from an Archive) appcompat_v7.zip
5. Import Existing Project (from an Archive) DroidNxtDistanceTracker.zip
6. Place phone in Development Mode
7. Run DroidNxtDistanceTracker as an Android App
8. Select you phone or emulator to see app (Note bluetooth won't work with emulator)


##Lab Instructions:


1. Register a handler to log messages from the NXT. (See MainFragment.java for an example of this)
2. Determine commands you want to run for: request distance messurement, request status, read distance and inputs for the motors. (See NXTTalker.java)
3. Create Distance response class response class
4. Connect value from Distance Response class to the distance value on the MainFragment
5. Add logic for starting and stopping based on distance.

