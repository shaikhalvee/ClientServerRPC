# PROJECT 1: CLIENT SERVER COMMUNICATIONS AND REMOTE PROCEDURE CALLS
## Project Structure

This is the project for **Client Server Communication and Remote Procedure Calls** assignment, or the __Project 1__.
It has 6 parts: 
- TCP ([TCPServer.java](src/TCPServer.java), [TCPClient.java](src/TCPClient.java))
- UDP ([UDPServer.java](src/UDPServer.java), [UDPClient.java](src/UDPClient.java))
- Multithreaded TCP ([MultiThreadedTCPServer.java](src/MultiThreadedTCPServer.java), [MultiThreadedTCPClient.java](src/MultiThreadedTCPClient.java))
- Multithreaded UDP ([MultiThreadedUDPServer.java](src/MultiThreadedUDPServer.java), [MultiThreadedUDPClient.java](src/MultiThreadedUDPClient.java))
- Synchronous RPC ([SyncRPCServer.java](src/SyncRPCServer.java), [SyncRPCClient.java](src/SyncRPCClient.java))
- Asynchronous RPC ([AsyncRPCServer.java](src/AsyncRPCServer.java), [AsyncRPCClient.java](src/AsyncRPCClient.java))

### Folder Structure

```arduino
ClientServerRPC
 ┣ image
 ┃ ┗ server
 ┃ ┗ client 
 ┗ src
 ┗ README.md
 ┣ out
 ┃ ┗ production
 ┃   ┗ ClientServerRPC
```
The `image/server` folder has the images. When client requests a file named in the server folder to the respective
server, the server sends back the file with renamed version. Then the client saves it in the `image/client`
folder. And the `out/production/ClientServerRPC` contains the class files to run.

## Project Execution [Compile & Run]
1. Unzip the `ClientServerRPC.zip` file.
2. Traverse to the project root with `cd /your/path/to/ClientServerRPC`
3. [N.B.] The current folder should be in the root `ClientServerRPC` folder, not the `src` folder.
4. Compile the java files: `javac -d out/production/ClientServerRPC src/*.java`. This should compile all the required
java files into class file.
5. Run required files by `java -cp out/production/ClientServerRPC <Classname>`.
For example, if you want to run the `TCPServer`, the run `java -cp out/production/ClientServerRPC TCPServer`
Similarly for client, open a new terminal window, and run `java -cp out/production/ClientServerRPC TCPClient`
6. For Multithreaded, Sync RPC and Async RPC, please do the following
   1. For multithreaded
      1. Run the server: `java -cp out/production/ClientServerRPC MultiThreadedTCPServer`
      2. Run the client: `java -cp out/production/ClientServerRPC MultiThreadedTCPClient 25`, here `25` is the number of loop, where we have 5 files. So total thread would be `5*25=125 threads`
   2. For Sync RPC:
      1. Run the server: `java -cp out/production/ClientServerRPC SyncRPCServer`
      2. Run the client: `java -cp out/production/ClientServerRPC SyncRPCClient 6 999900000`, here `6` is the number of loop, and `999900000` is the number of value/iterations for foo.
   3. 1. Run the server: `java -cp out/production/ClientServerRPC AsyncRPCServer`
      2. Run the client: `java -cp out/production/ClientServerRPC AsyncRPCClient 6 999900000`, here `6` is the number of loop, and `999900000` is the number of value/iterations for foo.







