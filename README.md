# Phoebe - A StickyPolicyMiddlewareFYP

Phoebe is a p2p console based chat application using a sticky policy privacy method to attempt user privacy.
The goal of Phoebe is to create a Java based multi user P2P chat app, that allows users to send messages and images without fear of privacy infringments.

  By using a JSON based sticky policy, any message sent in DMs follows a strict logging method showing: who has opened the message, if it has been edited/when, 



  
  What is a Sticky Policy Middleware?

  - A sticky policy is a piece of data that 'sticks' with other data being sent. In this case when a user attempts to write a message to another user, fields must be set on the JSON being sent to ensure it is correctly sending the data alongside the logs needed. 

23.02.25

Current plans for addition, a basic GUI for viewing any data sent between users similar to a simple QBT style application using JavaFX.

Have started to change I.O elements -> Nio for better file handling stuff. 

Added a new switch case for the actual receiving of data from other users, allows for more specific instructions on whats read etc than before.

Added a slight frame for other file support than just images / text messages. Nio sees any raw data as the same so its practicially the same code for PDF, PNG, JPEG etc just actually ensuring these are read correctly is a different case.
  - With this fact, want to automate the data as to "what" file type the data actually is. Saw a useful website on how to get this **https://www.geeksforgeeks.org/java/how-to-get-the-file-extension-in-java/** however currently not implemented due to the "what if data is not actually this file type".
  - Write about above security flaw IF actually added to code base in the report ...

  








Issues -- 
If two or more Peers have the same username how do you define who the peer is? How do you stop this from occuring

How to make sure a user can not corupt the privacy elements of the code by pretending to be another user i.e. IP spoof etc.

How to make the file an EXE - better than making it just a cmd always compile project and more user friendly for testing etc.
