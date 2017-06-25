package com.test.chatserver;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

public class MongoConnectingTest {
    public static void main (String[] args) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("Examples");
        MongoCollection<Document> collection = database.getCollection("people");
        
        System.out.println(mongoClient.getAddress());
        return;
        
        /*
        List<Integer> books = Arrays.asList(27464, 747854);
        DBObject person = new BasicDBObject("_id", "jo")
                                    .append("name", "Jo Bloggs")
                                    .append("address", new BasicDBObject("street", "123 Fake St")
                                                                 .append("city", "Faketon")
                                                                 .append("state", "MA")
                                                                 .append("zip", 12345))
                                    .append("books", books);
        
        Document personDocument = Document.parse(person.toString());
        
        collection.insertOne(personDocument);*/
        
    }
}