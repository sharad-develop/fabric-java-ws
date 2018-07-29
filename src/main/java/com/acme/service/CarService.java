package com.acme.service;

import com.acme.model.AcmeCar;
import com.acme.model.AcmeUser;
import com.acme.store.AcmeStore;
import com.google.protobuf.ByteString;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class holds the logic to interact with hyperledger
 */
@Service
public class CarService {

    private final String HFCAClient_ADDRESS = "http://localhost:7054";
    private final String AFFILIATION = "org1";
    private final String MSP_ID = "Org1MSP";
    private final String PEER_NAME = "peer0.org1.example.com";
    private final String PEER_URL = "grpc://localhost:7051";
    private final String ORDERER_NAME = "orderer.example.com";
    private final String ORDERER_URL = "grpc://localhost:7050";
    private final String EVENTHUB_NAME = "eventhub01";
    private final String EVENTHUB_URL = "grpc://localhost:7053";
    private final String CHANNEL_NAME = "mychannel";
    private final String CC_NAME = "fabcar";
    private final long TRANSACTION_TIMEOUT = 30;


    private Channel channel;
    @Autowired
    private AcmeStore acmeStore;

    private static final Logger log = Logger.getLogger(CarService.class);


    /**
     * Get or create admin.
     * Preregistered admin only needs to be enrolled with Fabric caClient.
     *
     * This method first verifies if the admin is already enrolled. If not it is enrolled with fabric-ca.
     * @param name
     * @param password
     * @return
     * @throws Exception
     */
    public AcmeUser getOrCreateAdmin(String name, String password) throws Exception {
        //check if admin exists
        AcmeUser admin = getUser(name);

            if(admin == null){
                //enroll admin
                Enrollment enrollment = getHFCAClient().enroll(name, password);
                admin = new AcmeUser(name, AFFILIATION, MSP_ID, enrollment);
                acmeStore.put(admin);
            }

        return admin;
    }

    /**
     * Get or create user. Users need to be registered AND enrolled.
     *
     * This method first verifies if user is already enrolled. If not it is enrolled with fabric-ca.
     * @param adminName
     * @param userName
     * @return
     * @throws Exception
     */
    public AcmeUser getOrCreateUser(String adminName, String userName) throws Exception {
        AcmeUser acmeUser = getUser(userName);
        if(acmeUser == null){
            HFCAClient hfcaClient = getHFCAClient();
            AcmeUser admin = getUser(adminName);
            RegistrationRequest registrationRequest = new RegistrationRequest(userName, AFFILIATION);

            String enrollmentSecret = hfcaClient.register(registrationRequest, admin);
            Enrollment enrollment = hfcaClient.enroll(userName, enrollmentSecret);
            acmeUser = new AcmeUser(userName, AFFILIATION, MSP_ID, enrollment);
            acmeStore.put(acmeUser);
        }

        return acmeUser;
    }

    /**
     * Query to return all cars from blockchain.
     *
     * @return
     * @throws Exception
     */
    public List<AcmeCar> queryAllFabcars() throws Exception {

        List<AcmeCar> carList = new ArrayList<>();

        Collection<ProposalResponse> proposalResponseCollection = queryFabCar("queryAllCars");

        proposalResponseCollection.forEach(proposalResponse -> {

            ByteString payload = proposalResponse.getProposalResponse().getResponse().getPayload();
            try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(payload.toByteArray()))) {
                JsonArray arr = jsonReader.readArray();
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject rec = arr.getJsonObject(i);
                    carList.add(getCarFromRecord(rec));
                }


            }
        });

        return carList;
    }

    /**
     * Query to return a car based on key specified.
     *
     * @param key
     * @return
     * @throws Exception
     */
    public List<AcmeCar> queryFabcar(String key) throws Exception {

        List<AcmeCar> carList = new ArrayList<>();
        Collection<ProposalResponse> proposalResponseCollection = queryFabCar("queryCar", new String[] {key});

        proposalResponseCollection.forEach(proposalResponse -> {

            ByteString payload = proposalResponse.getProposalResponse().getResponse().getPayload();

            try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(payload.toByteArray()))) {
                AcmeCar car= getCarJsonObject(jsonReader.readObject());
                car.setKey(key);
                carList.add(car);
            }
        });

        return carList;
    }

    /**
     * This method instantiates the chaincode to add a car to the blockchain.
     *
     * @param car
     * @return
     * @throws Exception
     */
    public String createCar(AcmeCar car) throws Exception {

        HFClient hfClient = getHFClient();
        hfClient.setUserContext(getUser("admin"));

        Channel channel = getChannel(hfClient);

        CompletableFuture<BlockEvent.TransactionEvent> transaction = createAndSendTransaction(hfClient, channel, car);
        BlockEvent.TransactionEvent transactionEvent = transaction.get(TRANSACTION_TIMEOUT, TimeUnit.SECONDS);

        String transactionId = transactionEvent.getTransactionID();

        if(transactionEvent.isValid()){
            log.info("Transaction :"+transactionId+ " successful on:"+transactionEvent.getTimestamp());
            log.info("Car added successfully to fabcar n/w");

        }else{
            log.info("Transaction :"+transactionId+ " failed on:"+transactionEvent.getTimestamp());
        }

        return transactionId;


    }

    /**
     * This method constructs a channel from a already configured channel.
     * Peer, orderer and eventhub need to be specified for the channel.
     *
     * @param hfClient
     * @return
     * @throws CryptoException
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    private Channel constructChannel(HFClient hfClient) throws CryptoException, InvalidArgumentException, TransactionException {


        Peer peer = hfClient.newPeer(PEER_NAME, PEER_URL);
        Orderer orderer = hfClient.newOrderer(ORDERER_NAME, ORDERER_URL);
        EventHub eventHub = hfClient.newEventHub(EVENTHUB_NAME, EVENTHUB_URL);

        Channel channel = hfClient.newChannel(CHANNEL_NAME);
        channel.addPeer(peer).addOrderer(orderer).addEventHub(eventHub).initialize();

        return channel;

    }

    /**
     * Get or construct a channel
     *
     * @param hfClient
     * @return
     * @throws InvalidArgumentException
     * @throws TransactionException
     * @throws CryptoException
     */
    private Channel getChannel(HFClient hfClient) throws InvalidArgumentException, TransactionException, CryptoException {
        if(channel == null){
            channel = constructChannel(hfClient);
        }

        return channel;
    }

    /**
     *
     * @param fcn
     * @param args
     * @return
     * @throws Exception
     */
    private Collection<ProposalResponse> queryFabCar(String fcn, String... args) throws Exception {

        HFClient hfClient = getHFClient();
        hfClient.setUserContext(getUser("admin"));

        Channel channel = getChannel(hfClient);

        QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CC_NAME).build();
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        queryByChaincodeRequest.setFcn(fcn);
        queryByChaincodeRequest.setArgs(args);

        Collection<ProposalResponse> proposalResponseCollection = channel.queryByChaincode(queryByChaincodeRequest);

        return proposalResponseCollection;
    }


    /**
     * Send transaction to the hyperledger n/w.
     *
     * @param hfClient
     * @param channel
     * @param car
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     */
    private CompletableFuture<BlockEvent.TransactionEvent> createAndSendTransaction(HFClient hfClient, Channel channel, AcmeCar car)
            throws InvalidArgumentException, ProposalException {

       ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CC_NAME).build();

       TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
       transactionProposalRequest.setChaincodeID(chaincodeID);
       transactionProposalRequest.setFcn("createCar");
       transactionProposalRequest.setArgs(new String[]{car.getKey(),car.getMake(), car.getModel(), car.getColour(), car.getOwner()});

       Collection<ProposalResponse> proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
       List<ProposalResponse> invalidResponse = proposalResponses.stream()
                                                            .filter(r -> r.isInvalid())
                                                            .collect(Collectors.toList());
        if (!invalidResponse.isEmpty()) {
            invalidResponse.forEach(response -> {
                log.error(response.getMessage());
            });

            throw new RuntimeException("Proposal not accepted. Invalid response found");
        }

        return channel.sendTransaction(proposalResponses);

    }

    private HFCAClient getHFCAClient() throws MalformedURLException, IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient hfcaClient = HFCAClient.createNewInstance(HFCAClient_ADDRESS, getHFCAClientProperties());
        hfcaClient.setCryptoSuite(cryptoSuite);

        return hfcaClient;
    }

    private HFClient getHFClient() throws CryptoException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);

        return hfClient;
    }



    private AcmeUser getUser(String name) throws Exception {
        return acmeStore.get(name);

    }

    /**
     * Get ca properties. There is nothing specific for this sample app
     * @return
     */
    private Properties getHFCAClientProperties(){
        return null;
    }


    /**
     * Create model object from key/value record pair
     * @param rec
     * @return
     */
    private AcmeCar getCarFromRecord(JsonObject rec) {
        String key = rec.getString("Key");
        JsonObject carRec = rec.getJsonObject("Record");
        AcmeCar car = getCarJsonObject(carRec);
        car.setKey(key);
        return car;
    }

    /**
     * Create model object from json
     * @param json
     * @return
     */
    private AcmeCar getCarJsonObject(JsonObject json) {
        String color = json.getString("colour");
        String make = json.getString("make");
        String model =  json.getString("model");
        String owner = json.getString("owner");

        AcmeCar car = new AcmeCar(color, make, model, owner);

        return car;

    }
}
