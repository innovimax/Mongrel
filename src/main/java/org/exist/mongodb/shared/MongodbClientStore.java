package org.exist.mongodb.shared;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.xquery.XPathException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.exist.mongodb.xquery.MongodbModule.MONGO_ID;

/**
 * @author wessels
 */
public class MongodbClientStore {

    protected final static Logger LOG = LogManager.getLogger(MongodbClientStore.class);

    private static MongodbClientStore instance = null;
    private final Map<String, MongoClientWrapper> clients = new HashMap<>();

    public static synchronized MongodbClientStore getInstance() {
        if (instance == null) {
            instance = new MongodbClientStore();
        }
        return instance;
    }

    public void add(String id, MongoClient client, String username) {
        MongoClientWrapper wrapper = new MongoClientWrapper(id, client, username);
        clients.put(id, wrapper);
    }

    public void remove(String mongodbClientId) {
        clients.remove(mongodbClientId);
    }

    public Set<String> list() {
        return clients.keySet();
    }

    public MongoClient get(String mongodbClientId) {

        MongoClientWrapper clientwrapper = clients.get(mongodbClientId);
        if (clientwrapper != null) {
            return clientwrapper.getMongoClient();
        }
        return null;
    }

    public boolean isValid(String mongodbClientId) {
        return get(mongodbClientId) != null;
    }

    public String create(String url, String username) {

        // Construct client
        MongoClientURI uri = new MongoClientURI(url);
        MongoClient client = new MongoClient(uri);

        LOG.debug(String.format("client: %s", client));

        // Create unique identifier
        String mongodbClientId = UUID.randomUUID().toString();

        // Register
        add(mongodbClientId, client, username);

        return mongodbClientId;
    }

    public MongoClient validate(String mongodbClientId) throws XPathException {
        if (mongodbClientId == null || !isValid(mongodbClientId)) {
            try {
                // introduce a delay
                Thread.sleep(1000L);

            } catch (InterruptedException ex) {
                LOG.error(ex);
            }
            throw new XPathException(MONGO_ID, null);
        }

        MongoClientWrapper clientwrapper = clients.get(mongodbClientId);
        if (clientwrapper != null) {
            return clientwrapper.getMongoClient();
        }
        return null;

    }

    class MongoClientWrapper {
        // TODO: use information to make connections tracable

        private String mongodbClientId;
        private MongoClient client;
        private String username;
        private XMLGregorianCalendar calendar;

        public MongoClientWrapper(String mongodbClientId, MongoClient client, String username) {
            this.mongodbClientId = mongodbClientId;
            this.client = client;
            this.username = username;

            try {
                calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            } catch (DatatypeConfigurationException ex) {
                //
            }
        }

        public String getMongodbClientId() {
            return mongodbClientId;
        }

        public void setMongodbClientId(String mongodbClientId) {
            this.mongodbClientId = mongodbClientId;
        }

        public MongoClient getMongoClient() {
            return client;
        }

        public void setMongoClient(MongoClient client) {
            this.client = client;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public XMLGregorianCalendar getCalendar() {
            return calendar;
        }

        public void setCalendar(XMLGregorianCalendar calendar) {
            this.calendar = calendar;
        }

    }
}
