# ExpireMap Implementation

## Overview
This project implements an `ExpireMap`, a concurrent map where entries automatically expire after a specified timeout period.

## Flow of the Program

### Initialization
1. **Class Declaration**: The `ExpireMapImpl` class implements the `ExpireMap` interface.
2. **Table Initialization**: An array of lists (`table`) is initialized to store `CustomMap` objects.
   
   ```java
   this.table = new LinkedList[INITIAL_CAPACITY];
   for (int i = 0; i < INITIAL_CAPACITY; i++) {
       table[i] = new LinkedList<>();
   }
 4. **Start Cleanup Thread**: A daemon thread is started to periodically clean up expired entries.
    
    ```
    startCleanupThread();
    ```
## Methods

### put(K key, V value, long timeoutMs)
- Lock Acquisition: The method acquires a lock to ensure thread safety.
- Hash Calculation: The key's hash is calculated to determine the index in the table.
- Bucket Retrieval: The bucket (list) at the calculated index is retrieved.
- Entry Update: If an entry with the same key exists, it is updated; otherwise, a new entry is added.
- Table Resize: If the table is more than 75% full, it is resized.

### get(K key)
- Lock Acquisition: The method acquires a lock to ensure thread safety.
- Hash Calculation: The key's hash is calculated to determine the index in the table.
- Bucket Retrieval: The bucket (list) at the calculated index is retrieved.
- Entry Retrieval: The entry is retrieved if it exists and is not expired; otherwise, null is returned.

### remove(K key)
- Lock Acquisition: The method acquires a lock to ensure thread safety.
- Hash Calculation: The key's hash is calculated to determine the index in the table.
- Bucket Retrieval: The bucket (list) at the calculated index is retrieved.
- Entry Removal: The entry with the specified key is removed if it exists.

## Cleanup Thread
**Periodic Cleanup**: The daemon thread periodically checks and removes expired entries from the table.

## Additional Tests
The program includes several tests to validate the correctness of the implementation:

- Basic Put and Get
  - **Description**: This test verifies the basic functionality of inserting a key-value pair into the ExpireMap and retrieving it.
  - **Example**:
    
     ```
     expiremap.put("key1", "value1", 1000);
     assert "value1".equals(expiremap.get("key1"));
     ```

- Expiration
  - **Description**: This test checks if entries in the ExpireMap expire after the specified timeout period.
  - **Example**:

    ```
    expiremap.put("key2", "value2", 500); // expires after 500 milliseconds
    TimeUnit.MILLISECONDS.sleep(600); // wait for 600 milliseconds
    assert expiremap.get("key2") == null;
    ```
- Remove
  - **Description**: This test ensures that an entry can be removed from the ExpireMap and is no longer retrievable afterward.
  - **Example**:

    ```
    expiremap.put("key3", "value3", 1000);
    expiremap.remove("key3");
    assert expiremap.get("key3") == null;
    ```
- Concurrent Put and Get
   - **Description**: This test verifies that the ExpireMap can handle concurrent put and get operations without data corruption.
   - **Example**:

      ```
      Thread thread1 = new Thread(() -> expiremap.put("key4", "value4", 1000));
      Thread thread2 = new Thread(() -> assert "value4".equals(expiremap.get("key4")));
      thread1.start();
      thread2.start();
      thread1.join();
      thread2.join();
      ```
- Concurrent Put and Expiration
   - **Description**: This test checks if entries put concurrently into the ExpireMap still expire correctly after their timeout period.
   - **Example**:

     ```
       Thread thread1 = new Thread(() -> expiremap.put("key5", "value5", 500));
       Thread thread2 = new Thread(() -> {
          try { TimeUnit.MILLISECONDS.sleep(600); } 
          catch (InterruptedException e) { e.printStackTrace(); }
          assert expiremap.get("key5") == null;
       });
       thread1.start();
       thread2.start();
       thread1.join();
       thread2.join();
     ```
- Concurrent Put and Remove
   - **Description**: This test ensures that entries put concurrently into the ExpireMap can be removed correctly.
   - **Example**:

      ```
      Thread thread1 = new Thread(() -> expiremap.put("key6", "value6", 1000));
      Thread thread2 = new Thread(() -> expiremap.remove("key6"));
      thread1.start();
      thread2.start();
      thread1.join();
      thread2.join();
      assert expiremap.get("key6") == null;
      ```
- Concurrent put and expiration and remove
  - **Description**: This test checks the scenario where an entry is put into the ExpireMap, allowed to expire, and then removed concurrently. It ensures that the entry can be expired and removed properly even when these actions occur in parallel.
  - **Example**:

     ```
      // Thread 1 puts a key-value pair into the map with a 500 millisecond timeout
      Thread thread1 = new Thread(() -> expiremap.put("key7", "value7", 500));
      
      // Thread 2 waits for 600 milliseconds (ensuring the entry has expired) and then tries to remove it
      Thread thread2 = new Thread(() -> {
          try {
              TimeUnit.MILLISECONDS.sleep(600); // Wait for entry to expire
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          expiremap.remove("key7"); // Attempt to remove the expired entry
      });
      
      thread1.start();
      thread2.start();
      thread1.join();
      thread2.join();
      
      // Check if the entry is correctly handled (it should be null since it should have expired and been removed)
      assert expiremap.get("key7") == null;
     ```

- Replace Existing Entry
  - **Description**: This test verifies that an existing entry in the ExpireMap can be replaced with a new value and the old value is no longer retrievable.
  - **Example**:

     ```
       expiremap.put("key1", "newValue1", 1000);
       assert "newValue1".equals(expiremap.get("key1"));
     ```
## Screenshot of output

<a href="https://github.com/Manishn18/AssignmentTask/tree/main/src/com/Assignment" target="_blank">Link to Source Code</a>

![Output_Img](https://github.com/user-attachments/assets/c431f258-bb45-4905-86e6-035630e0a96d)

