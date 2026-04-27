# Phoebe - A Java P2P based, Sticky Policy File transfer and messaging application.

Phoebe is a p2p console based chat application using a sticky policy privacy enforcement mechansim to attempt ensure privacy.
The goal of Phoebe is to create a Java based multi user P2P chat app with an internal TEE, that allows users to send messages and images without fear of privacy infringments.

  By using a JSON based sticky policy system, any form of communication sent with specific commands, has an attached policy JSON which allows the sender to specify :
   <ul>  
     <li> If a message can be read by a User </li>
      <li> How long until a message expires - In UTC time</li>
       <li> If a message is a File or Image, if it can be downloaded by a User </li>
   </ul> 
Regardless of if it is communication from commands or simply users conversing with each other, all messages follow a strict Curve25519 algorithm based E2EE Structure, as inspired by the Signal and Session messaging applications.  

# Motive : 
  
Phoebe intends to allow users to send files between themselves in a P2P enviroment, by utilising a JSON based sticky policy method.


<img width="1039" height="365" alt="image" src="https://github.com/user-attachments/assets/32b4f414-2974-4e90-b126-5e83af800933" />


Meta data found attatched to the data sent is then processed once a user recieves it to follow a set of Application layer commands, which are then enforced by a local enforcement checker.




  
 ## What is a Sticky Policy?

  - A sticky policy is a piece of data that 'sticks' with other data being sent. In this case when a user attempts to write a message to another user, fields must be set on the JSON being sent to ensure it is correctly sending the data alongside the logs needed. 
  


 - See the following resources for futher related information :
     -     https://ieeexplore.ieee.org/abstract/document/6297922
     -     https://www.dicom.uninsubria.it/~sabrina.sicari/public/documents/journal/2019_StickyPolicies.pdf
     -     https://ieeexplore.ieee.org/abstract/document/8807248
     -     https://ieeexplore.ieee.org/abstract/document/7794325 

  ****Sharellf**** the last resource mentioned, is a key research and implementation inspiration for this project. Portions of this project is directly inspired by it in terms of security flaw acknowledments alongside some expiration related functionality. 





# Features
  
  Phoebe allows users to message each other in group a public setting or in DM message fully E2ee. 
    [Add screenshot of it]

  Phoebe allows for Images and Files to be sent to other users in DM settings, currently no group image or file sharing is possible.
  When sending an image or file, they are rendered in RAM using JavaFX in the InboxFx class.
  By rendering these images and files, it prevents a user needed to risk data being downloaded and forwarded onwards to more users.

  Example -- 
  <img width="1920" height="1080" alt="photomode_25042026_013004" src="https://github.com/user-attachments/assets/6a844929-20f4-4224-a9cb-f779b17587ed" />
  [The above image is the downloaded image with no rendering.]

  <img width="799" height="679" alt="image" src="https://github.com/user-attachments/assets/59f8cc45-47c4-4072-8fe6-94b428a28849" />
  [Image rendered with JavaFX, featuring the senders name, File name and Watermark over the image.]


  
  

  - Phoebe hosts a variety of commands that allow Users to specify what style of message or action they desire.
  - A simplified Distributed Hash table which allows users to merge tables with each other and update them to have additional users present.
    


# Usage

  ``` 
  git clone https://github.com/CelestialSirens/StickyPolicyMiddlewareFYP

  cd StickyPolicyMiddlewareFYP

  java run Phoebe.java 
  ```
  After Phoebe is running, a user MUST know a bootstrap nodes information to be able to communicate with them.
  

  To use Phoebe some prerequisites are needed. 
  First a user must have downloaded this repository, and have JAVA 21 installed, as Phoebe uses this to run. As Maven is with the repository it should already install all other dependencies required to run Phoebe once the Repository is installed. 
    









