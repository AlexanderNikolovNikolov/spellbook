package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.core.model.SyncStats;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the synchronization between the desktop Spellbook app
 * and its web edition.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class SynchronizeService extends AbstractPersistenceService {
    private static final String UPDATE_URL = "http://spellbook.drow.com/update/";

    private static final SynchronizeService INSTANCE = new SynchronizeService();

    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    public static SynchronizeService getInstance() {
        return INSTANCE;
    }

    public Map<DictionaryEntry, DictionaryEntry.State> retrieveUpdatedEntries() {
        Map<DictionaryEntry, DictionaryEntry.State> updatedEntries = new HashMap<DictionaryEntry, DictionaryEntry.State>();

        try {
            URL updateUrl = new URL(UPDATE_URL + (getLastSyncDate().getTime() / 1000) + ".xml");

            BufferedInputStream in = new BufferedInputStream(updateUrl.openStream());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("new-word");
            System.out.println("Information of all suggestions");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node firstNode = nodeLst.item(s);

                if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                    DictionaryEntry dictionaryEntry = new DictionaryEntry();

                    Element firstElement = (Element) firstNode;
                    NodeList dictionaryNodeList = firstElement.getElementsByTagName("dictionary");
                    Element dictionaryElement = (Element) dictionaryNodeList.item(0);
                    NodeList dictionary = dictionaryElement.getChildNodes();
                    final String dictionaryName = ((Node) dictionary.item(0)).getNodeValue();
                    System.out.println("Dictionary : " + dictionaryName);
                    dictionaryEntry.setDictionary(DICTIONARY_SERVICE.getDictionary(dictionaryName));

                    NodeList wordNodeList = firstElement.getElementsByTagName("word");
                    Element wordElement = (Element) wordNodeList.item(0);
                    NodeList word = wordElement.getChildNodes();

                    final String wordValue = ((Node) word.item(0)).getNodeValue();
                    System.out.println("Word : " + wordValue);
                    dictionaryEntry.setWord(wordValue);

                    NodeList translationNodeList = firstElement.getElementsByTagName("translation");
                    Element translationElement = (Element) translationNodeList.item(0);
                    NodeList translation = translationElement.getChildNodes();

                    final String translationValue = ((Node) translation.item(0)).getNodeValue();
                    System.out.println("Translation : " + translationValue);
                    dictionaryEntry.setTranslation(translationValue);

                    NodeList stateNodeList = firstElement.getElementsByTagName("state");
                    Element stateElement = (Element) stateNodeList.item(0);
                    NodeList state = stateElement.getChildNodes();

                    final String stateValue = ((Node) state.item(0)).getNodeValue();
                    System.out.println("State : " + stateValue);

                    updatedEntries.put(dictionaryEntry, DictionaryEntry.State.valueOf(stateValue.toUpperCase()));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updatedEntries;
    }

    public void pullUpdates() {
        Map<DictionaryEntry, DictionaryEntry.State> updateEntries = retrieveUpdatedEntries();

        for (Map.Entry<DictionaryEntry, DictionaryEntry.State> tDictionaryEntryStateEntry : updateEntries.entrySet()) {
            DictionaryEntry de = tDictionaryEntryStateEntry.getKey();
            DictionaryEntry.State state = tDictionaryEntryStateEntry.getValue();

            if (state == DictionaryEntry.State.NEW) {
                DICTIONARY_SERVICE.addWord(de.getWord(), de.getTranslation(), de.getDictionary());
            } else if (state == DictionaryEntry.State.UPDATED) {
                DICTIONARY_SERVICE.updateWord(de.getWord(), de.getTranslation(), de.getDictionary());
            }
        }

        SyncStats syncStats = new SyncStats();
        syncStats.setPulledEntries(updateEntries.values().size());
        syncStats.setPushedEntries(0);

    }

    public void pushUpdates() {
        List<DictionaryEntry> localChanges = getLocalChanges();
    }

    public List<DictionaryEntry> getLocalChanges() {
        //return EM.createQuery("select de from DictionaryEntry de where de.updatedByUser = true").getResultList();

        return null;
    }

    public int getNumberOfLocalChanges() {
        return getLocalChanges().size();
    }

    public Date getLastSyncDate() {
//        List<SyncStats> syncStats = EM.createQuery("select ss from SyncStats ss order by ss.created desc").getResultList();
//
//        System.out.println("Sync stats size " + syncStats.size());
//
//        if (syncStats.isEmpty()) {
//            return null;
//        } else {
//            return syncStats.get(0).getCreated();
//        }
        return null;
    }
}
