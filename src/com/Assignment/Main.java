package com.Assignment;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExpireMap<String, String> expiremap = new ExpireMapImpl<>();

        // Test 1: Basic put and get
        expiremap.put("key1", "value1", 1000);
        System.out.println("Test 1: " + ("value1".equals(expiremap.get("key1")) ? "Basic put and get test Passed" : "Basic put and get test Failed"));
        System.out.println("The obtained value of key1-value1 pair is: "+expiremap.get("key1"));
        System.out.println();

        // Test 2: Expiration
        expiremap.put("key2", "value2", 500); // 500 milliseconds
        System.out.println("Before expiration the value of value2: "+expiremap.get("key2"));
        TimeUnit.MILLISECONDS.sleep(600); // Sleep for 600 milliseconds
        System.out.println("After expiration the value of value2: "+expiremap.get("key2"));
        System.out.println("Test 2: " + (expiremap.get("key2") == null ? "Expiration test Passed" : "Expiration test Failed"));
        System.out.println();

        // Test 3: Remove
        expiremap.put("key3", "value3", 1000);
        System.out.println("Before removing the value of value3: "+expiremap.get("key3"));
        expiremap.remove("key3");
        System.out.println("After removing the value of value3: "+expiremap.get("key3"));
        System.out.println("Test 3: " + (expiremap.get("key3") == null ? "Remove test Passed" : "Remove test Failed"));
        System.out.println();

        // Test 4: Concurrent put and get
        Thread thread1 = new Thread(() -> expiremap.put("key4", "value4", 1000));
        Thread thread2 = new Thread(() -> {
            System.out.println("Test 4: " + ("value4".equals(expiremap.get("key4")) ? "Concurrent put and get test Passed" : "Concurrent put and get test Failed"));
            System.out.println();
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Test 5: Concurrent put and expiration
        Thread thread5 = new Thread(() -> {
            expiremap.put("key6", "value6", 500); // 500 milliseconds
        });
        Thread thread6 = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(600); // Sleep for 600 milliseconds
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Test 5: " + (expiremap.get("key6") == null ? "Concurrent put and expiration test Passed" : "Concurrent put and expiration test Failed"));
            System.out.println();
        });
        thread5.start();
        thread6.start();
        thread5.join();
        thread6.join();

        // Test 6: Concurrent put and remove
        Thread thread3 = new Thread(() -> expiremap.put("key5", "value5", 1000));
        Thread thread4 = new Thread(() -> expiremap.remove("key5"));
        thread3.start();
        thread4.start();
        thread3.join();
        thread4.join();
        System.out.println("Test 6: " + (expiremap.get("key5") == null ? "Concurrent remove test Passed" : "Concurrent remove test Failed"));
        System.out.println();

        // Test 7: Concurrent put and expiration and remove
        Thread thread7 = new Thread(() -> {
            expiremap.put("key7", "value7", 500); // 500 milliseconds
        });
        Thread thread8 = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(600); // Sleep for 600 milliseconds
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            expiremap.remove("key7");
        });
        thread7.start();
        thread8.start();
        thread7.join();
        thread8.join();
        System.out.println("Test 7: " + (expiremap.get("key7") == null ? "Concurrent put and expiration and remove test Passed" : "Concurrent put and expiration and remove test Failed"));
        System.out.println();

        // Test 8: Replace existing entry
        expiremap.put("key8", "initialValue", 1000);
        System.out.println("Initial value of the key8 is: "+expiremap.get("key8"));
        expiremap.put("key8", "newValue", 1000);
        System.out.println("New value of the key8 is: "+expiremap.get("key8"));
        System.out.println("Test 8: " + ("newValue".equals(expiremap.get("key8")) ? "Replace existing entry test Passed" : "Replace existing entry test Failed"));
        System.out.println();
    }
}
