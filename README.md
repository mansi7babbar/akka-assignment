#Akka Assignment No.1

Q1. There is a company called XYZ that processes various log files from the operating system and provides analysis and recommendation on the basis of the data collected from the files. 

A. Write a program that can take directory path is input and read all the files and provide the following info:

- Number of total errors, warning info

- Average errors per file

B. Write a software program using the Akka actor model that can read from the directory with embarrassingly parallel and asynchronous fashion and provide analysis ASAP. (N number of Akka actors are allowed)

C. Analyze the performance using visual VM and provide performance and memory comparison between the above approaches. 

#Akka Assignment No.2
 
Q1. Update the existing Akka program to do the following tasks:

- Use different dispatchers (IO dispatchers with fixed thread pool) while reading from files. 

- Limit the number of actors that should be configurable by using a router.

- Use a mailbox having a capacity of 1000 files. More files should be discarded with an error log. 

#Akka Assignment No.3

Q1. Update outcome of the second assignment according to the following requirements:

- Apply supervision strategy for all the actors. If an actor fails it should stop processing the message.

- Implement a scheduler that will read from the file every 5 minutes and display the results. 
