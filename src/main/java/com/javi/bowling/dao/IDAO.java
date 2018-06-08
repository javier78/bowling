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

    /**
     * Inserts object E into the database
     * @param row The row to be inserted. The id field should not be set when invoking this method
     * @return the id for the row inserted into the database
     */
    int insert(E row);

    /**
     * Updates the row object in the database
     * @param row The row object to be updated
     * @return true if the update succeeded, false otherwise.
     */
    boolean update(E row);
}
