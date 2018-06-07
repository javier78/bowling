package com.javi.bowling.dao;

import java.util.List;

/**
 * Interface for a DAO to facilitate communication with the database. E will refer to the DAO's corresponding model class
 * @param <E> The DAO's corresponding model class.
 */
public interface IDAO<E> {
    /**
     * Finds all the records in the model's table.
     * @return A Collection of model objects consisting of all records in the table.
     */
    List<E> findAll();

    /**
     * Finds the record with the given id.
     * @param id the value to search on.
     * @return The record with the given id
     */
    E findById(int id);

//    /**
//     * Finds all the records that satisfy the given predicate
//     * @param predicate the predicate to filter on
//     * @return A Collection of filtered records
//     */
//    List<E> where(String predicate);
}
