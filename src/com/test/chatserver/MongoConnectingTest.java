package com.test.chatserver;



import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import org.bson.Document;


import java.util.Arrays;
import java.util.List;

/**
 * This is a tester class for a Mongo DB. Just want to make sure everything works.
 * 
 * 
 * @author joey
 *
 */

public class MongoConnectingTest {
    public static void main (String[] args) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("Examples");
        MongoCollection<Document> collection = database.getCollection("people");
        
        System.out.println(mongoClient.getAddress());
        DBObject query = new BasicDBObject("author", "Dude");
        MongoIterable<Document> cursor = collection.find(Document.parse(query.toString()));
        Document jo = cursor.first();
        System.out.println(jo.get("message"));
        Gson gson = new Gson();
        TimeChatMessage lastMsg = new TimeChatMessage("Dude", "Where's my car");
        String json = gson.toJson(lastMsg);
        
        //collection.insertOne(Document.parse(json));
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