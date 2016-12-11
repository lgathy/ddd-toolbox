package com.doctusoft.ddd.model;

/**
 * All type-safe task class needs to implement this interface. If one also implements {@link java.io.Serializable}, it
 * will be sent using Java Serialization, otherwise in text format using its {@link #toString()} method.
 */
public interface Task {}
