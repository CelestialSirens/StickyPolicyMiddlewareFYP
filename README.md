# Phoebe - A Java based, Sticky Policy File transfer application

Phoebe is a p2p console based chat application using a sticky policy privacy method to attempt user privacy.
The goal of Phoebe is to create a Java based multi user P2P chat app, that allows users to send messages and images without fear of privacy infringments.

  By using a JSON based sticky policy, any message sent in DMs follows a strict logging method showing: who has opened the message, if it has been edited/when, 

# Motive : 
  
Phoebe intends to allow users to send files between themselves in a P2P enviroment, by utilising a JSON based sticky policy method.


<img width="779" height="296" alt="image" src="https://github.com/user-attachments/assets/0c38bc14-c029-4991-9d34-d58066352b13" />

Meta data found attatched to the data sent is then processed once a user recieves it to follow a set of Application layer commands.




  
 ## What is a Sticky Policy?

  - A sticky policy is a piece of data that 'sticks' with other data being sent. In this case when a user attempts to write a message to another user, fields must be set on the JSON being sent to ensure it is correctly sending the data alongside the logs needed. 
  


 - See the following resources for futher related information :
     -     https://ieeexplore.ieee.org/abstract/document/6297922
     -     https://www.dicom.uninsubria.it/~sabrina.sicari/public/documents/journal/2019_StickyPolicies.pdf
     -     https://ieeexplore.ieee.org/abstract/document/8807248
     -     https://ieeexplore.ieee.org/abstract/document/7794325 

  ****Sharellf**** the last resource mentioned, is a key research and implementation inspiration for this project. Portions of this project is directly inspired by it in terms of security flaw acknowledments alongside some expiration related functionality. 





# Features
  - Allows for either group based or 1 - 1 file transfer
  - Allows a robust selection of file types to be transfered including  PNG, PDF, JPEG, [ADD OTHERS WHEN DONE]
  - Features a basic GUI [Show proof when done]




# Usage




















## Personal notes 
  
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
