package rss.input;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

//xml parsing libraries
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

public class uriReader {

    public ArrayList<uriObject> xmlFileParser() {
        boolean isFeedHeader = true;
        objectList objectList = new objectList();
        String channel = "";
        String link = "";
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader =
                    factory.createXMLEventReader(new FileReader("feedUri.xml"));

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    String localPart = event.asStartElement().getName()
                            .getLocalPart();
                    switch (localPart) {
                        case "item":
                            if (isFeedHeader) {
                                isFeedHeader = false;
                            }
                            event = eventReader.nextEvent();
                            break;
                        case "channelName":
                            channel = getCharacterData(event,eventReader);
                            break;
                        case "url":
                            link = getCharacterData(event, eventReader);
                            break;
                    }
                } else if (event.isEndElement()) {
                    if (event.asEndElement().getName().getLocalPart().equals("item")) {
                        uriObject object = new uriObject();
                        object.setChannel(channel);
                        object.setLink(link);
                        objectList.setObject(object);
                        event = eventReader.nextEvent();
                    }
                }
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(XMLStreamException e){
            throw new RuntimeException(e);
        }

        return objectList.getObjects();
    }

    private static String getCharacterData(XMLEvent event, XMLEventReader eventReader)
            throws XMLStreamException {
        String result = "";
        event = eventReader.nextEvent();
        if (event instanceof Characters) {
            result = event.asCharacters().getData();
        }
        return result;
    }
}
