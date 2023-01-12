package battleship.model;

import org.stringtree.json.JSONReader;
import org.stringtree.json.JSONWriter;

public interface IParcelable<E> {
	
	public Object toParcelableObject();
	public E fromParcelableObject(Object obj);
	
	public default String toJSON() {
		return new JSONWriter().write(toParcelableObject());
	}
	
	public default E fromJSON(String json) {
		return (E)fromParcelableObject(new JSONReader().read(json));
	}
}
