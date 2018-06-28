# LEAP_JARVIS
Using Leap Motion, emulate the JARVIS command center from IRON MAN.

Also incorporates Google Text to Speech API (WIP).

2/26/18
I first started this poject back in 8th grade (2013). It's been a while since I have worked on it but the code is still functional though, the Leap SDK has updated since then and some functionality is depreciated. The main missing component not in the git is the LEAP SDK which you can download at the LEAP Motion website. 

Google Text-Speech is functional but very slow. The intent was to create an voice-controlled system that could launch OS programs such as Chrome and to initiate fundamental OS commands like changing volume or closing certain programs.

Although made mainly with Java, C++ was needed to facilitate OS system calls such as volume control and moving windows around with LEAP. At the time, I knew little C++ and was more interested in the cross-platform capabilities of Java. I used JNA to jerry-rig a combination of C++ and Java together to create a Frankenstein program which surprisingly works.
