package de.epiceric.justmoney.storage;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import de.epiceric.justmoney.model.BankAccount;

/**
 * Interface for a bank account storage class.
 * 
 * @since 1.0
 */
public interface BankStorage {
    /**
     * Gets the name of the storage type.
     * 
     * @return the storage type name
     * @since 1.0
     */
    String getTypeName();

    /**
     * Stores the given bank account.
     * 
     * @param account the account to store
     * @return a future that completes when the account has been stored
     * @since 1.0
     */
    CompletableFuture<Void> storeAccount(BankAccount account);

    /**
     * Gets all stored bank accounts.
     * 
     * @return a future that completes with the stored bank accounts when they have been loaded
     * @since 1.0
     */
    CompletableFuture<Collection<BankAccount>> getAccounts();
}