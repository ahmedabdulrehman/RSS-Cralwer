package rss.input;

import java.util.ArrayList;

public class objectList extends ArrayList<uriObject> {

    final ArrayList<uriObject> entries = new ArrayList<uriObject>();

    private void objectList (){}

    public void setObject(uriObject object) {
        entries.add(object);
    }

    public ArrayList<uriObject> getObjects() {
        return entries;
    }
}
