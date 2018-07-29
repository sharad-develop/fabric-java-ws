package com.acme.store;

import com.acme.model.AcmeUser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sample store to hold user data. Actual implementation should replace this ldap/db.
 * In this app all the user data will be stored at HFC_KEY_STORE
 */
@Service
public class AcmeStore {


    private final String HFC_KEY_STORE = "hfc_key_store";

    /**
     * Deserialize AcmeUser object from file
     *
     * @param name The name of the user. Used to build file name ${name}.jso
     * @return
     * @throws Exception
     */
    public AcmeUser get(String name) throws Exception {
        if (Files.exists(Paths.get(HFC_KEY_STORE,name + ".json"))) {
            try (ObjectInputStream decoder = new ObjectInputStream(
                    Files.newInputStream(Paths.get(HFC_KEY_STORE,name + ".json")))) {
                return (AcmeUser) decoder.readObject();
            }
        }
        return null;

    }

    /**
     * Serialize AcmeUser object to file
     *
     *
     * @throws IOException
     */
    public void put(AcmeUser acmeUser) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(HFC_KEY_STORE,acmeUser.getName() + ".json")))) {
            oos.writeObject(acmeUser);
        }
    }
}
