# zk-client
light zookeeper client program

## usage
### distributed task lock

- example:
```
Lock lock = ClientFactory.getZKClient("127.0.0.1", "2181").createLock("testTestTestLockPath_test001");
if (lock.getLock()) {
    // doing the task ... ...
}
lock.releaseLock();
```

### distributed leader selector

- example:
Before process was started, initialize the leader selector.
```
ClientFactory.getZKClient("127.0.0.1", "2181").leaderInit();
```

After process started, get the flag whether current process is leader.
```
boolean isLeader = Leader.isLeader();
if(isLeader){
    // doing the task ... ...
}
```