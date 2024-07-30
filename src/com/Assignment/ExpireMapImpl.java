package com.Assignment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// Implements the ExpireMap interface
public class ExpireMapImpl<K, V> implements ExpireMap<K, V> {
    private List<CustomMap<K, V>>[] table;  // This is an array of lists where each index of table[i] contains LinkedList<CustomMap<K, V>> holding CustomMap objects
    private static final int INITIAL_CAPACITY = 16; // Initial capacity of the table
    private int size; // Current size of the table
    private final ReentrantLock lock = new ReentrantLock(); // ReentrantLock for thread safety of the table

    @SuppressWarnings("unchecked") // To avoid the unchecked cast warning
    // Constructor to initialize table and size variables and start the cleanup thread in the background
    public ExpireMapImpl() {
        this.table = new LinkedList[INITIAL_CAPACITY]; // The entire array of lists (buckets) that holds all the entries, initialized with INITIAL_CAPACITY = 16

        // Initialize each bucket (slot in the table) as an empty LinkedList, meaning each index in the table will hold a LinkedList of CustomMap<K, V> objects for handling multiple CustomMap<K, V> objects with the same key (collisions)
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            table[i] = new LinkedList<>(); // A specific list (or bucket) at index i in the table array, where multiple CustomMap objects are stored if they hash to the same index.
        }

        this.size = 0; // Initializes size to 0
        startCleanupThread(); // Starts the cleanup thread in the background to remove expired entries from the table after timeoutMs has passed since they were added to the table
    }

    // Hash function to get the index in the table where the key should be stored
    private int hash(K key) {
        return (key == null) ? 0 : Math.abs(key.hashCode()) % table.length; // Returns the hash code of the key or 0 if key is null
    }

    // If there is no entry with the key in the map, add the key/value pair as a new entry
    // If there is an existing entry with the key, the current entry will be replaced with the new key/value pair
    // If the newly added entry is not removed after timeoutMs since it's added to the map, remove it
    @Override
    public void put(K key, V value, long timeoutMs) {
        lock.lock(); // Lock the table and size variables to ensure thread safety
        try {
            int hash = hash(key); // Calculate the hash value of the key
            long expiryTime = System.currentTimeMillis() + timeoutMs; // Calculate the expiration time of the entry
            List<CustomMap<K, V>> bucket = table[hash]; // Get the bucket at the calculated index

            // Check if there is an entry with the same key in the bucket and if so, replace it with the new key/value pair and update the expiration time of the entry else add a new entry
            for (CustomMap<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    entry.value = value; // Update the value of the entry
                    entry.expiryTime = expiryTime; // Update the expiration time of the entry
                    return; // Return without adding a new entry
                }
            }

            bucket.add(new CustomMap<>(key, value, expiryTime)); // Add a new entry to the bucket
            size++;

            // If the table is more than 75% full, resize the table to double its size
            if (size > table.length * 0.75) {
                resize(); // resize the table
            }
        } finally {
            lock.unlock(); // Unlock the table and size variables
        }
    }

    // Get the value associated with the key if present; otherwise, return null
    @Override
    public V get(K key) {
        lock.lock(); // Lock the table and size variables to ensure thread safety
        try {
            int hash = hash(key); // Calculate the hash value of the key
            List<CustomMap<K, V>> bucket = table[hash]; // Get the bucket at the calculated index of the table
            long currentTime = System.currentTimeMillis(); // Get the current time in milliseconds

            // Check if there is an entry with the same key in the bucket and if so, return its value else return null
            for (CustomMap<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    if (entry.expiryTime > currentTime) {
                        return entry.value; // Return the value if the entry is not expired
                    } else {
                        bucket.remove(entry);
                        size--;
                        return null; // Return null if the entry is expired
                    }
                }
            }
            return null; // Return null if there is no entry with the same key in the bucket
        } finally {
            lock.unlock(); // Unlock the table and size variables
        }
    }

    // Remove the entry associated with key, if any
    @Override
    public void remove(K key) {
        lock.lock(); // Lock the table and size variables to ensure thread safety
        try {
            int hash = hash(key); // Calculate the hash value of the key
            List<CustomMap<K, V>> bucket = table[hash]; // Get the bucket at the calculated index
            bucket.removeIf(entry -> entry.key.equals(key)); // Remove the entry with the same key from the bucket
            size--; // Decrement the size of the table
        } finally {
            lock.unlock(); // Unlock the table and size variables
        }
    }

    // Resizes the table to double its size if the table is more than 75% full (for the INITIAL_CAPACITY = 16, 75% of 16 = 12)
    @SuppressWarnings("unchecked") // To avoid the unchecked cast warning
    private void resize() {
        List<CustomMap<K, V>>[] oldTable = table; // Save the old table
        table = new LinkedList[oldTable.length * 2]; // Create a new table with double the size

        // Copy the entries from the old table to the new table
        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
        }

        size = 0; // Reset the size of the table

        // Copy the entries from the old table to the new table
        for (List<CustomMap<K, V>> bucket : oldTable) {
            // For each bucket in the old table, copy its entries to the new table
            for (CustomMap<K, V> entry : bucket) {
                put(entry.key, entry.value, entry.expiryTime - System.currentTimeMillis()); // Add the entry to the new table
            }
        }
    }

    // Start a thread to periodically clean up expired entries
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Wait for 1 second before checking for expired entries
                    lock.lock(); // Lock the table and size variables to ensure thread safety
                    try {
                        long currentTime = System.currentTimeMillis(); // Get the current time in milliseconds

                        // For each bucket in the table, remove any expired entries
                        for (List<CustomMap<K, V>> bucket : table) {
                            bucket.removeIf(entry -> entry.expiryTime <= currentTime);
                        }
                    } finally {
                        lock.unlock(); // Unlock the table and size variables
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // If the thread is interrupted, set the interrupted flag
                }
            }
        });
        cleanupThread.setDaemon(true); // Set the thread as a daemon thread to be terminated when the JVM exits
        cleanupThread.start(); // Start the thread to clean up expired entries every 1 second (1000 milliseconds) until the thread is interrupted or terminated
    }
}
