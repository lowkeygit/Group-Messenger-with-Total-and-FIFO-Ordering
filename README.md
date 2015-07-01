## Group Messenger with Total and FIFO Ordering Guarantees on Android
* The app is a Group Messenger that multicasts a message to all app instances and store them in a permanent key-value storage. (Multicasts also includes the one that is sending the message)
* The app includes ordering guarantees which covers total ordering as well as FIFO ordering. (Ordering implies the receiving order based on the Lamport logical clocks)
* The messages were assigned sequence numbers in order to provide total and FIFO ordering guarantees. 
* A content provider is implemented using SQLite on Android to store key-value pair.
* App uses Basic-multicast. It does not implement Relaible-multicast.
* Reference for message ordering. [Here](http://www.cs.uic.edu/~ajayk/Chapter6.pdf).
