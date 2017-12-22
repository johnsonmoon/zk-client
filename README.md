# zk-client
light zookeeper client program

## Update Information
- 1.0.1
Leader selector zookeeper reconnection.

## Usages
### 1.zk server node data viewer
- run the viewer
```
windows java -classpath .;%CLASSPATH%;  xuyihao.zk.client.ZKViewer
linux   java -classpath .:$CLASSPATH:  xuyihao.zk.client.ZKViewer
```
- console examples
```
Input "help" for more ... ...
help
Commands: 
-------------------
host  -  setting zk host
port  -  setting zk port
-------------------
help  -  print   helps 
clear -  clear   screen 
cls   -  clear   screen 
exit  -  exit    the program 
-------------------
child -  print   children  of an existing zookeeper node 
data  -  print   data      of an existing zookeeper node 

host 127.0.0.1
Host [127.0.0.1] set.
port 2181
Port [2181] set.

child /
Zookeeper Connected
------------------------------------------------------
Path [/] exists: [true]
-----------------
Path [/] children:
ZKLeaderTestNode
Leader
message_email_notify_lock
testTestTestLockPath_test011
ZKLockTestLock
testTestTestLockPath_test112
dubbo
testsLockTwo
TestLockOneThreadLock
testThreadLock
ZKLeader
testsLockOne
TestLockThreeThreadLock
TestLockTwoThreadLock
lock
testLockThree
zookeeper
testLockTwo
testTestTestLockPath_test001
testTestTestLockPath_test002
testTestTestLockPath_test003
testTestTestLockPath_test004
ZKLeaderTwo
ZKLock_Lock
eagle
ZKLeader_Leader
testLock

------------------------------------------------------

exit
Bye ...
```

### 2. distributed task lock

- example:
```
Lock lock = ClientFactory.getZKClient("127.0.0.1", "2181").createLock("testTestTestLockPath_test001");
if (lock.getLock()) {
    // doing the task ... ...
}
lock.releaseLock();
```

### 3. distributed leader selector

- example:
Before process was started, initialize the leader selector.
```
ClientFactory.getZKClient("127.0.0.1", "2181").leaderInit();
```

After process started, get the flag whether current process is leader.
```
boolean isLeader = ClientFactory.getZkClient().isLeader();
```