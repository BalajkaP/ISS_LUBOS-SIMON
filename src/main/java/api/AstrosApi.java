package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crud.AddToDB;
import crud.DeleteFromDB;
import crud.ReadFromDB;
import crud.UpdateDB;
import org.hibernate.Session;
import org.hibernate.Transaction;
import database.DbConnect;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// POPIS PROGRAMU
//It creates a URL object representing the URL of the API endpoint (http://api.open-notify.org/astros.json).
//It establishes an HttpURLConnection to make an HTTP GET request to the URL.
//It checks the response code to ensure a successful request (HTTP 200 OK).
//If the response code is 200 (OK), the program proceeds to read the response data from the API:
//
// VYSVĚTLENÍ ŘÁDKU S BUFFERED READER
//The code you're referring to is responsible for setting up a BufferedReader to read data from an input stream obtained from an HttpURLConnection.
//It reads the response line by line and appends each line to a StringBuilder to construct the complete response.
// connection.getInputStream(): This part of the code obtains the input stream from an HttpURLConnection object, which represents the stream
// of data from the HTTP response. The getInputStream() method is a standard method provided by the HttpURLConnection class. It allows you to
// access the input stream associated with the connection, which contains the response data from the HTTP server.
//new InputStreamReader(connection.getInputStream()): The InputStreamReader is used to convert the raw binary data from the input stream into
// character data. It acts as a bridge between byte streams and character streams, allowing you to read text data. In this case, it's used to
// convert the binary data from the HTTP response into character data. This is necessary because the BufferedReader reads text data, and the
// response data is usually in a character format.
//new BufferedReader(...): Finally, a BufferedReader is created, and it's wrapped around the InputStreamReader.
// The BufferedReader is used to efficiently read text from the character stream. It buffers the input, making reading more efficient.
// It provides methods like readLine() to read lines of text from the stream.
//So, the entire line of code sets up a BufferedReader to read the text data from the HTTP response. It first converts the binary input stream
// from the HTTP response to character data using an InputStreamReader and then wraps it with a BufferedReader to efficiently read and process
// the text data line by line. This is a common pattern for reading text data from web services, files, or any other source where data is
// transmitted as bytes and needs to be processed as text.

//It closes the reader and disconnects the HTTP connection.
//The code then uses the Jackson library's ObjectMapper to parse the JSON response into a JsonNode object, which can be easily navigated and extracted.
//
//It extracts the number of astronauts in space from the JSON response.
//It retrieves the list of astronauts along with their respective spacecraft.
//
//POZOR: Je zde SPRÁVNÁ STRUKTURA KÓDU, I JAK MÁ BÝT SPRÁVNĚ DATABASE SESSION, TRANSACTION.
//       A TAKY CONNECTION OPEN A CLOSE PRO HTTP CONNECTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

public class AstrosApi {
    public static void main(String[] args) {
        try {
            // Create a URL object for the API endpoint
            URL url = new URL("http://api.open-notify.org/astros.json");
            // Establish an HTTP connection to the URL
            // Type casting (HttpURLConnection): If we want to work with the specific features and methods of HttpURLConnection,
            // which is a subclass of URLConnection specialized for HTTP connections.
            // openConnection(): vrací URLConnection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //establishes an HttpURLConnection to make an HTTP GET request to the URL.
            connection.setRequestMethod("GET");
            // Get the HTTP response code
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {  // Check for a successful response (HTTP 200 OK)
                // Create a BufferedReader to read the API response. It reads the response line by line and appends each
                // line to a StringBuilder to construct the complete response.
                // The code you're referring to is responsible for setting up a BufferedReader to read data from an input stream obtained from an HttpURLConnection.
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                // Read the response line by line and build a complete response
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                //It closes the reader and disconnects the HTTP connection, PROTOŽE UŽ MÁM VŠE POTŘEBNÉ NAČTENÉ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                reader.close();
                connection.disconnect();
                // Parse the JSON response using Jackson's ObjectMapper
                // This code involves the use of the Jackson library, a popular library in the Java ecosystem for working with JSON data
                //ObjectMapper: The ObjectMapper is a class provided by the Jackson library. It's used for mapping between JSON data and Java objects.
                // Essentially, it provides methods to serialize Java objects into JSON and deserialize JSON into Java objects. In this line,
                // an ObjectMapper object named objectMapper is created, which will be used to parse the JSON response from the API.
                ObjectMapper objectMapper = new ObjectMapper();
                //objectMapper.readTree(response.toString()): The objectMapper instance is used to read the JSON response text and parse it into a
                // Jackson JsonNode object. The readTree method is a convenient way to parse a JSON string into a structured tree-like representation that
                // can be easily navigated and accessed.
                //response.toString(): The response is a StringBuilder that contains the JSON response as a string. The toString() method converts
                // the StringBuilder into a regular string.
                //objectMapper.readTree(...): This method parses the JSON text and constructs a JsonNode object representing the entire JSON structure.
                // A JsonNode is a part of Jackson's object model for working with JSON and represents a node within the JSON structure. It can be an object,
                // an array, a string, a number, etc., depending on the structure of the JSON.
                //So, rootNode is now a JsonNode object that contains the entire JSON structure from the API response. You can use methods and properties of
                // JsonNode to navigate and extract specific data from the JSON, whether it's reading values from JSON objects or iterating through JSON arrays.
                //This code is a crucial step in the process of handling JSON data received from an API. It deserializes the JSON response into a structured object
                // that can be easily manipulated and accessed in your Java code.
                JsonNode rootNode = objectMapper.readTree(response.toString());
                // Extract the number of astronauts in space. To "number" je součástí JSON souboru- je to na konci.
                int number = rootNode.get("number").asInt();
    //          System.out.println("Počet ľudí v medzinárodnej vesmírnej stanici: " + number);
                // Extract the list of astronauts. Stejně jako "number" přečtu celý objekt "people" a uložím do people
                JsonNode people = rootNode.get("people");
                // Establish a database session and begin a transaction
                // SESSION TEDY OTEVŘU ZDE, PROTOŽE ZDE ZAČNU VOLAT JEDNOTLIVÉ DB OPERACE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                Session session = DbConnect.getSession();
                Transaction transaction = session.beginTransaction();
                // Iterate through the astronauts and their spacecraft
                for (JsonNode person : people) {
                    // It extracts gradually the astronaut's name and the name of the spacecraft they are on.
                    String name = person.get("name").asText();
                    String craft = person.get("craft").asText();
                    //System.out.println(name + " na palube " + craft);

                    // Add the spacecraft to the database
                    AddToDB.addCraft(session, craft);
                    // Add the astronaut to the database
                    AddToDB.addAstronaut(session, name, craft);
                }

                //DeleteFromDB.deleteAll(session); //mazanie celej tabulky
                //DeleteFromDB.deleteAstronaut(session,"Gui Haichow"); //mazanie podla mena astronauta
                //DeleteFromDB.deleteCraftwithAstronauts(session,"ISS"); //odstranuje lod aj s astronautami ktore su na nej

                //UpdateDB.updateAstronautName(session, "Gui Haichow","Howard J. Wolowitz"); // update mena astronauta
                //UpdateDB.updateCraftName(session,"ISS","Medzinarodna vesmirna stanica"); //update mena lode
                //UpdateDB.updateCraftofAstronaut(session,"Gui Haichow","SpaceX"); //zmení loď astronautovi,ak nova lod este neexistuje prida novu


                transaction.commit();
                // POZOR: PRO READ=SELECT NETŘEBA DĚLAT COMMIT, PROTOŽE NIC NEMĚNÍM A TÍM PÁDEM NEDĚLÁM NIC NEBEZPEČNÉHO!!!!!!!!!!!!
                //ReadFromDB.printAllAstrosWithCraft(session);
                //ReadFromDB.printAstroById(session, 1);
                //ReadFromDB.printAstroByCraft(session);
                //ReadFromDB.printAstroByName(session);
                session.close();                           // ending the database connection
            } else {
                // Print an error message if the response code is not 200
                System.out.println("Chyba pri získavaní dát. Kód odpovede: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
