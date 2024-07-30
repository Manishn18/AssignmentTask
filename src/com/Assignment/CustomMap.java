package com.Assignment;

// This is a custom class that holds a key-value pair (K key and V value) and an expiration time (long expiryTime)
class CustomMap<K, V> {
    K key;
    V value;
    long expiryTime;

// Constructor for the CustomMap class that initializes a key, value, and expiration time
    public CustomMap(K key, V value, long expiryTime) {
        this.key = key;
        this.value = value;
        this.expiryTime = expiryTime;
    }
}
