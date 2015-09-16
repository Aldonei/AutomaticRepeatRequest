# AutomaticRepeatRequest

Download e import into Eclipse IDE to check the source code.
It was a extra work (Master Degree) for Telecom Lecture in Griffith College Dublin in 2005.

Automatic Repeat reQuest - ARQ
The ARQ simulator is a simple java-based simulator of ARQ (Automatic Repeat reQuest) protocols. 
It currently supports three such protocols: 
Stop and Wait (SAW), Go back N (GBN) and Selective Repeat (SR). 
The simulator assumes a point-to-point communication and provides probabilistic simulation of the selected protocol given a 
variety of input parameters specifying such things as network characteristics, error probabilities, and transmission 
characteristics. Being a probabilistic simulation (and using Java’s built-in random number generation facilities) it is best to 
run each test case you are interested in several times and average the results. 
The user interface is very simple. 
1. Latency – the network latency (i.e. the delay to deliver 1 byte in one direction) an integer specifying the latency in milliseconds.
Data rate – the network data rate (number of bytes that can be sent in a second): an integer specifying the number of bytes 
transmitted per second.
2. numFrame – the number of frames the sender is to send: an integer.
3. frameSize – the frame size: an integer specifying the number of bytes) in each frame.
4. interFrameDelay – the inter-frame delay (i.e. the time that must elapse between frames sent by the sender): an integer specifying 
the inter-frame delay in milliseconds.
5. errProb – the probability of an error occurring in any transmitted byte: a float (between 0.0 and 1.0) where 0.0 means an 
error will never occur and 1.0 means an error will always occur. Choosing 1.0 will result in constant re-sends and a 
non-terminating simulation so don’t specify 1.0.
6. dropP – the probability of any frame being dropped: a float (between 0.0 and 1.0). The values are interpreted in the same 
way as for errP.
