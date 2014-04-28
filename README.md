Homework management app for Android
===================================

^ That is what this is. More details to come. Eventually.

How to build
------------
Start by importing the grade(ient) project into Eclipse. The rest of the steps are to get the datetimepicker dependency building.

1. Open a terminal and `cd [repository root]/externals/datetimepicker` (replace `[repository root]` with the actual path to wherever you put the repository)
2. In the terminal, `git submodule init`
3. In the terminal, `git submodule update`
4. Import datetimepicker project into Eclipse
5. Right click project --> Properties --> Android --> make sure something is checked under "Project Build Target" (if nothing is checked, choose your most recent Android version)
6. Link `android-support-v4.jar` to the project. One way to do this is to reuse the version of the library from the main grade(ient) project: go to Properties --> Java Build Path --> Libraries tab --> Add JARs... --> choose grade(ient)/libs/android-support-v4.jar.
